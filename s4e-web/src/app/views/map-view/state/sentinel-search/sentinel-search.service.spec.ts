import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {SentinelSearchService} from './sentinel-search.service';
import {SentinelSearchStore} from './sentinel-search.store';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {SentinelSearchQuery} from './sentinel-search.query';
import {SentinelSearchFactory, SentinelSearchMetadataFactory} from './sentinel-search.factory.spec';
import {createSentinelSearchResult, SENTINEL_SELECTED_QUERY_KEY, SENTINEL_VISIBLE_QUERY_KEY} from './sentinel-search.model';
import {NotificationService} from 'notifications';
import {Router} from '@angular/router';
import {ModalService} from '../../../../modal/state/modal.service';
import {SENTINEL_SEARCH_RESULT_MODAL_ID} from '../../sentinel-search/search-result-modal/search-result-modal.model';
import environment from 'src/environments/environment';

describe('SentinelSearchResultService', () => {
  let service: SentinelSearchService;
  let query: SentinelSearchQuery;
  let store: SentinelSearchStore;
  let http: HttpTestingController;
  let modalService: ModalService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    service = TestBed.get(SentinelSearchService);
    query = TestBed.get(SentinelSearchQuery);
    store = TestBed.get(SentinelSearchStore);
    http = TestBed.get(HttpTestingController);
    modalService = TestBed.get(ModalService);
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
      service.getSentinels();
      http.expectNone(`${environment.apiPrefixV1}/config/sentinel-search`);
      http.verify();
    });

    it('should call http and set loadings', async () => {
      const metadata = SentinelSearchMetadataFactory.build();
      const promise = service.getSentinels();
      const r = http.expectOne({method: 'GET', url: `${environment.apiPrefixV1}/config/sentinel-search`});
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
      const params = {param1: 'abc'};
      const searchResults = SentinelSearchFactory.buildList(3);
      const promise = service.search(params).toPromise();
      const r = http.expectOne({method: 'GET', url: `${environment.apiPrefixV1}/search?param1=abc`});
      r.flush(searchResults);
      await promise;
      expect(query.getValue().loaded).toBeTruthy();
      expect(query.getValue().loading).toBeFalsy();
      expect(query.getAll()).toEqual(searchResults.map(el => createSentinelSearchResult(el)));
      http.verify();
    });

    it('should set errors and show notification', async () => {
      const notificationService = TestBed.get(NotificationService);
      const spy = spyOn(notificationService, 'addGeneral').and.stub();
      const promise = service.search({}).toPromise();
      const r = http.expectOne({method: 'GET', url: `${environment.apiPrefixV1}/search`});
      r.flush({}, {status: 400, statusText: 'Bad Request'});
      await promise.catch(() => {});
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
      service.search({});
      expect(query.getAll().length).toBe(0);
    });
  });

  it('openModalForResult', () => {
    const spy = spyOn(modalService, 'show');
    const result = createSentinelSearchResult(SentinelSearchFactory.build());
    store.set([result]);
    service.openModalForResult(result.id);
    expect(query.getActiveId()).toEqual(result.id);
    expect(spy).toHaveBeenCalledWith({id: SENTINEL_SEARCH_RESULT_MODAL_ID, size: 'lg'});
  });

  it('nextActive', () => {
    const first = createSentinelSearchResult(SentinelSearchFactory.build());
    const second = createSentinelSearchResult(SentinelSearchFactory.build());
    store.set([first, second]);
    service.nextActive();
    expect(query.getActive()).toEqual(first);
    store.setActive(first.id);
    service.nextActive();
    expect(query.getActive()).toEqual(second);
    service.nextActive();
    expect(query.getActive()).toEqual(first);
  });

  it('previousActive', () => {
    const first = createSentinelSearchResult(SentinelSearchFactory.build());
    const second = createSentinelSearchResult(SentinelSearchFactory.build());
    store.set([first, second]);
    service.previousActive();
    expect(query.getActive()).toEqual(first);
    store.setActive(second.id);
    service.previousActive();
    expect(query.getActive()).toEqual(first);
    service.previousActive();
    expect(query.getActive()).toEqual(second);
  });
});
