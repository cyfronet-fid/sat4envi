/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {SentinelSearchService} from './sentinel-search.service';
import {SentinelSearchStore} from './sentinel-search.store';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {SentinelSearchQuery} from './sentinel-search.query';
import {SentinelSearchFactory, SentinelSearchMetadataFactory} from './sentinel-search.factory.spec';
import {
  createSentinelSearchResult,
  SENTINEL_PAGE_INDEX_QUERY_KEY,
  SENTINEL_SEARCH_ACTIVE_ID,
  SENTINEL_SEARCH_PARAMS_QUERY_KEY,
  SENTINEL_SELECTED_QUERY_KEY,
  SENTINEL_VISIBLE_QUERY_KEY
} from './sentinel-search.model';
import {NotificationService} from 'notifications';
import {Router} from '@angular/router';
import {ModalService} from '../../../../modal/state/modal.service';
import {SENTINEL_SEARCH_RESULT_MODAL_ID} from '../../sentinel-search/search-result-modal/search-result-modal.model';
import environment from 'src/environments/environment';
import {FormControl, FormGroup} from '@angular/forms';
import {of, ReplaySubject} from 'rxjs';
import {BACK_LINK_QUERY_PARAM} from '../../../../state/session/session.service';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {delay, take} from 'rxjs/operators';
import Spy = jasmine.Spy;
import * as akita from '@datorama/akita';

describe('SentinelSearchResultService', () => {
  let service: SentinelSearchService;
  let query: SentinelSearchQuery;
  let store: SentinelSearchStore;
  let http: HttpTestingController;
  let modalService: ModalService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    service = TestBed.get(SentinelSearchService);
    query = TestBed.get(SentinelSearchQuery);
    store = TestBed.get(SentinelSearchStore);
    http = TestBed.get(HttpTestingController);
    modalService = TestBed.get(ModalService);
    router = TestBed.get(Router);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('toggleSentinelVisibility', () => {
    const id = 'sentinel-id';
    const spy = spyOn(service, 'setSentinelVisibility').and.stub();
    const spy2 = spyOn(query, 'isSentinelVisible').and.returnValues(true, false);
    service.toggleSentinelVisibility(id);
    expect(spy).toHaveBeenCalledWith(id, false);
    service.toggleSentinelVisibility(id);
    expect(spy).toHaveBeenCalledWith(id, true);
  });

  it('setSentinelVisibility', () => {
    const sentinelId = 'sentinel-id';
    const router: Router = TestBed.get(Router);
    const spy = spyOn(router, 'navigate').and.stub();
    service.setSentinelVisibility(sentinelId, true);
    expect(spy).toHaveBeenCalledWith([], {
      queryParamsHandling: 'merge',
      queryParams: {
        [SENTINEL_VISIBLE_QUERY_KEY]: sentinelId,
        [SENTINEL_SELECTED_QUERY_KEY]: sentinelId,
      }, replaceUrl: true
    });
    service.setSentinelVisibility(sentinelId, false);
    expect(spy).toHaveBeenCalledWith([], {
      queryParamsHandling: 'merge',
      queryParams: {
        [SENTINEL_VISIBLE_QUERY_KEY]: '',
        [SENTINEL_SELECTED_QUERY_KEY]: '',
      }, replaceUrl: true
    });
  });

  describe('getSentinels', () => {
    it('should not call http.get if query.isMetadataLoaded() is true', () => {
      store.setMetadataLoaded();
      service.getSentinels$();
      http.expectNone(`${environment.apiPrefixV1}/config/search`);
      http.verify();
    });

    it('should call http and set loadings', async () => {
      const metadata = SentinelSearchMetadataFactory.build();
      const promise = service.getSentinels$().toPromise();
      const r = http.expectOne({method: 'GET', url: `${environment.apiPrefixV1}/config/search`});
      expect(query.isMetadataLoading()).toBeTruthy();
      r.flush(metadata);
      await promise;
      expect(query.isMetadataLoaded()).toBeTruthy();
      expect(query.isMetadataLoading()).toBeFalsy();
      expect(query.getValue().metadata).toEqual(metadata);
      http.verify();
    });
  });

  it('setLoaded should work', () => {
    service.setLoaded(true);
    expect(query.getValue().loaded).toBeTruthy();
    service.setLoaded(false);
    expect(query.getValue().loaded).toBeFalsy();
  });

  describe('search', () => {
    it('should work', async () => {
      const form = new FormGroup({common: new FormControl({})});
      form.get('common').setValue({param1: 'abc'});
      const router = TestBed.get(Router);
      const redirectPromise = of(null).toPromise();
      const spyRouter = spyOn(router, 'navigate').and.returnValue(redirectPromise);
      const searchResults = SentinelSearchFactory.buildList(3);
      const promise = service.search(form).toPromise();
      await redirectPromise;
      const r = http.expectOne({method: 'GET', url: `${environment.apiPrefixV1}/search?param1=abc&limit=15&offset=0`});
      r.flush(searchResults);
      await of(null).pipe(delay(500)).toPromise();
      const rCount = http.expectOne({method: 'GET', url: `${environment.apiPrefixV1}/search/count?param1=abc&limit=15&offset=0`});
      rCount.flush(13);
      await promise;
      expect(spyRouter).toHaveBeenCalledWith([], {
        queryParams: {
          searchParams: JSON.stringify({common: {param1: 'abc'}}),
          showSearchResults: '1',
          page: 0
        },
        queryParamsHandling: 'merge'
      });
      expect(query.getValue().loaded).toBeTruthy();
      expect(query.getValue().loading).toBeFalsy();
      expect(query.getAll()).toEqual(searchResults.map(el => createSentinelSearchResult(el)));
      http.verify();
    });

    it('should set errors and show notification', async () => {
      const notificationService = TestBed.get(NotificationService);
      const router = TestBed.get(Router);
      const redirectPromise = of(null).toPromise();
      const spyRouter = spyOn(router, 'navigate').and.returnValue(redirectPromise);
      const spy = spyOn(notificationService, 'addGeneral').and.stub();
      const promise = service.search(new FormGroup({})).toPromise();
      await redirectPromise;
      const r = http.expectOne({method: 'GET', url: `${environment.apiPrefixV1}/search?limit=15&offset=0`});
      r.flush({}, {status: 400, statusText: 'Bad Request'});
      await promise.catch(() => {
      });
      expect(spyRouter).toHaveBeenCalledWith([], {
        queryParams: {searchParams: '{}', showSearchResults: '1', page: 0},
        queryParamsHandling: 'merge'
      });
      expect(query.getValue().loaded).toBeFalsy();
      expect(query.getValue().loading).toBeFalsy();
      expect(query.getValue().error).not.toBeNull();
      expect(spy).toHaveBeenCalledWith({
        type: 'error',
        content: 'Wystąpił błąd podczas wyszukiwaniania'
      });

      http.verify();
    });

    it('should clear search results', () => {
      store.set([createSentinelSearchResult(SentinelSearchFactory.build())]);
      service.search(new FormGroup({}));
      expect(query.getAll().length).toBe(0);
    });
  });

  it('openModalForResult', () => {
    const spy = spyOn(router, 'navigate');
    const result = createSentinelSearchResult(SentinelSearchFactory.build());

    store.set([result]);
    service.openModalForResult(result.id);
    expect(spy).toHaveBeenCalledWith(
      [],
      {replaceUrl: true, queryParamsHandling: 'merge', queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: result.id}}
    );
  });

  it('connectQueryToActiveModal', async () => {
    const result = createSentinelSearchResult(SentinelSearchFactory.build());
    const result2 = createSentinelSearchResult(SentinelSearchFactory.build());
    store.set([result, result2]);

    const uuid = '4be89042a0';
    spyOn(akita, 'guid').and.returnValue(uuid);

    const params$ = new ReplaySubject(1);
    const routerQuery: RouterQuery = TestBed.get(RouterQuery);
    const spy = spyOn(modalService, 'show');
    const routerSpy = spyOn(routerQuery, 'selectQueryParams').and.returnValue(params$.asObservable());
    params$.next(result.id.toString());
    const activeModal$ = service.connectQueryToActiveModal();

    await activeModal$.pipe(take(1)).toPromise();
    expect(query.getActiveId()).toEqual(result.id);
    expect(spy).toHaveBeenCalledWith(
      {
        entity: null,
        id: 'search-result-modal-id',
        mode: 'sentinel',
        showNavigation: true,
        size: 'lg',
        uuid}
    );
    expect(routerSpy).toHaveBeenCalledWith(SENTINEL_SEARCH_ACTIVE_ID);
    params$.next(result2.id.toString());
  });

  it('nextActive', () => {
    const first = createSentinelSearchResult(SentinelSearchFactory.build());
    const second = createSentinelSearchResult(SentinelSearchFactory.build());
    const spy = spyOn(router, 'navigate');
    store.set([first, second]);
    service.nextActive();
    expect(spy).toHaveBeenCalledWith([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: first.id}
    });
    expect(query.getActive()).toEqual(first);
    store.setActive(first.id);
    service.nextActive();
    expect(spy).toHaveBeenCalledWith([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: second.id}
    });
    expect(query.getActive()).toEqual(second);
    service.nextActive();
    expect(query.getActive()).toEqual(first);
    expect(spy).toHaveBeenCalledWith([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: first.id}
    });
  });

  it('previousActive', () => {
    const first = createSentinelSearchResult(SentinelSearchFactory.build());
    const second = createSentinelSearchResult(SentinelSearchFactory.build());
    const spy = spyOn(router, 'navigate');
    store.set([first, second]);
    service.previousActive();
    expect(spy).toHaveBeenCalledWith([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: first.id}
    });
    expect(query.getActive()).toEqual(first);
    store.setActive(second.id);
    service.previousActive();
    expect(spy).toHaveBeenCalledWith([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: first.id}
    });
    expect(query.getActive()).toEqual(first);
    service.previousActive();
    expect(spy).toHaveBeenCalledWith([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: second.id}
    });
    expect(query.getActive()).toEqual(second);
  });

  it('clearResults', () => {
    const router: Router = TestBed.get(Router);
    store.set(SentinelSearchFactory.buildListSearchResult(2));
    const spy = spyOn(router, 'navigate');

    service.clearResults();
    expect(spy).toHaveBeenCalledWith([], {queryParamsHandling: 'merge', queryParams: {showSearchResults: undefined}});
    expect(query.getAll().length).toBe(0);
  });

  it('redirectToLoginPage', () => {
    const router: Router = TestBed.get(Router);
    const notificationService: NotificationService = TestBed.get(NotificationService);

    const spyRouter = spyOn(router, 'navigate');
    const spyNotification = spyOn(notificationService, 'addGeneral');
    const spyRouterQuery = spyOn((service as any).routerQuery as RouterQuery, 'getValue').and.returnValue({state: {url: '/map/abc'}});
    service.redirectToLoginPage();

    expect(spyRouter).toHaveBeenCalledWith(['/login'], {queryParams: {[BACK_LINK_QUERY_PARAM]: '/map/abc'}});
    expect(spyNotification).toHaveBeenCalledWith({type: 'warning', duration: 10000, content: 'Zaloguj się by uzyskać dostęp do zasobu'});
  });

  describe('connectQueryToForm', () => {
    let form: FormGroup;
    let routerQuery: RouterQuery;
    let params$: ReplaySubject<string[]>;
    let selectShowSearchResults$: ReplaySubject<boolean>;
    let spy: Spy;
    let spySearch: Spy;
    let searchParams: object;

    beforeEach(() => {
      form = new FormGroup({common: new FormControl({})});
      routerQuery = TestBed.get(RouterQuery);
      params$ = new ReplaySubject(1);
      selectShowSearchResults$ = new ReplaySubject(1);
      spyOn(query, 'selectShowSearchResults').and.returnValue(selectShowSearchResults$);
      selectShowSearchResults$.next(true);
      spy = spyOn(routerQuery, 'selectQueryParams').and.returnValue(params$);
      spySearch = spyOn(service, 'search').and.returnValue(of(null));
      searchParams = {common: {param1: 'abc'}};
      params$.next([JSON.stringify(searchParams), '0']);
    });

    it('success', async () => {
      await service.connectQueryToForm(form).toPromise();
      expect(form.value).toEqual(searchParams);
      expect(spySearch).toHaveBeenCalledWith(form, 0);
      expect(spy).toHaveBeenCalledWith([SENTINEL_SEARCH_PARAMS_QUERY_KEY, SENTINEL_PAGE_INDEX_QUERY_KEY]);
    });

    it('broken JSON', async () => {
      params$.next(['.broken / json', '0']);
      await service.connectQueryToForm(form).toPromise();
      expect(form.value).toEqual({common: {}});
      expect(spySearch).toHaveBeenCalledWith(form, 0);
      expect(spy).toHaveBeenCalledWith([SENTINEL_SEARCH_PARAMS_QUERY_KEY, SENTINEL_PAGE_INDEX_QUERY_KEY]);
    });

    it('should not search if drawer is not open', async () => {
      selectShowSearchResults$.next(false);
      await service.connectQueryToForm(form).toPromise();
      expect(spySearch).not.toHaveBeenCalled();
    });
  });
});
