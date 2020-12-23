/*
 * Copyright 2020 ACC Cyfronet AGH
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

import {environment} from '../../../../../environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SentinelSearchStore} from './sentinel-search.store';
import {
  createSentinelSearchResult,
  SENTINEL_PAGE_INDEX_QUERY_KEY,
  SENTINEL_PAGE_SIZE,
  SENTINEL_SEARCH_ACTIVE_ID,
  SENTINEL_SEARCH_PARAMS_QUERY_KEY,
  SENTINEL_SELECTED_QUERY_KEY,
  SENTINEL_SHOW_RESULTS_QUERY_KEY,
  SENTINEL_VISIBLE_QUERY_KEY,
  SentinelSearchResultResponse,
} from './sentinel-search.model';
import {SentinelSearchQuery} from './sentinel-search.query';
import {catchError, delay, filter, finalize, map, merge, pairwise, switchMap, take, tap} from 'rxjs/operators';
import {SentinelSearchMetadata} from './sentinel-search.metadata.model';
import {Router} from '@angular/router';
import {applyTransaction, HashMap} from '@datorama/akita';
import {NotificationService} from 'notifications';
import {ModalService} from '../../../../modal/state/modal.service';
import {makeDetailsModal, SENTINEL_SEARCH_RESULT_MODAL_ID} from '../../sentinel-search/search-result-modal/search-result-modal.model';
import {ActivatedQueue} from 'src/app/utils/search/activated-queue.utils';
import {Observable, of, throwError} from 'rxjs';
import {fromPromise} from 'rxjs/internal-compatibility';
import {FormGroup} from '@angular/forms';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {filterNotNull} from '../../../../utils/rxjs/observable';
import {BACK_LINK_QUERY_PARAM} from '../../../../state/session/session.service';
import {ModalQuery} from '../../../../modal/state/modal.query';

@Injectable({providedIn: 'root'})
export class SentinelSearchService {
  protected _activatedQueue: ActivatedQueue;

  constructor(
    private store: SentinelSearchStore,
    private query: SentinelSearchQuery,
    private http: HttpClient,
    private router: Router,
    private notificationService: NotificationService,
    private modalService: ModalService,
    private modalQuery: ModalQuery,
    private routerQuery: RouterQuery
  ) {
    this._activatedQueue = new ActivatedQueue(this.query, this.store);
  }

  setSentinelVisibility(sentinelId: string, visible: boolean) {
    this.router.navigate([], {
      queryParamsHandling: 'merge',
      queryParams: {
        [SENTINEL_VISIBLE_QUERY_KEY]: visible ? sentinelId : '',
        [SENTINEL_SELECTED_QUERY_KEY]: visible ? sentinelId : '',
      }, replaceUrl: true
    });
  }

  toggleSentinelVisibility(sentinelId: string) {
    this.setSentinelVisibility(sentinelId, !this.query.isSentinelVisible(sentinelId));
  }

  search(form: FormGroup, page: number = 0, refreshCount: boolean = true): Observable<SentinelSearchResultResponse[]> {
    const params: HashMap<any> = Object.values(form.value).reduce((prev, current) => Object.assign(prev, current), {});
    params.limit = SENTINEL_PAGE_SIZE;
    params.offset = page * SENTINEL_PAGE_SIZE;

    if (!!this.query.getValue().footprint) {
      Object.assign(params, {footprint: this.query.getValue().footprint});
    }

    applyTransaction(() => {
      if (refreshCount) {
        this.store.update({resultPagesCount: null, resultTotalCount: null});
      }
      this.store.set([]);
      this.store.update({loaded: false, loading: true, error: null});
    });

    return fromPromise(this.router.navigate([], {
      queryParams: {
        [SENTINEL_PAGE_INDEX_QUERY_KEY]: page,
        [SENTINEL_SEARCH_PARAMS_QUERY_KEY]: JSON.stringify(form.value),
        [SENTINEL_SHOW_RESULTS_QUERY_KEY]: '1'
      },
      queryParamsHandling: 'merge'
    }))
      .pipe(
        switchMap(() => this.http.get<SentinelSearchResultResponse[]>(
          `${environment.apiPrefixV1}/search`, {params})
        ),
        delay(250),
        map(data => data.map(product => createSentinelSearchResult(product))),
        tap(data => {
          this.store.set(data);
          this.store.setLoaded(true);
        }),
        switchMap(data => {
          return refreshCount
            ? this.http.get<any>(`${environment.apiPrefixV1}/search/count`, {params})
              .pipe(
                tap(count => this.store.update({resultTotalCount: count, resultPagesCount: Math.ceil(count / SENTINEL_PAGE_SIZE)})),
                map(() => data)
              )
            : of(data);
        }),
        catchError(error => {
          this.store.setError(error);
          this.notificationService.addGeneral({
            type: 'error',
            content: 'Wystąpił błąd podczas wyszukiwaniania'
          });
          return throwError(error);
        }),
        finalize(() => this.store.setLoading(false)),
      );
  }

  getSentinels$(): Observable<SentinelSearchMetadata> {
    if (this.query.isMetadataLoaded()) {
      return of(this.query.getValue().metadata);
    }

    this.store.setError(null);
    this.store.setMetadataLoading();
    const url = `${environment.apiPrefixV1}/config/sentinel-search`;
    return this.http.get<SentinelSearchMetadata>(url)
      .pipe(
        tap(() => this.store.setMetadataLoading(false)),
        tap(data => this.store.update({metadata: data, metadataLoaded: true})),
        catchError(error => {
          this.store.setError(error);
          this.notificationService.addGeneral({
            type: 'error',
            content: 'Wystąpił błąd podczas pobierania metadanych'
          });
          return throwError(error);
        })
      );
  }

  setLoaded(loaded: boolean) {
    this.store.setLoaded(loaded);
  }

  openModalForResult(resultId: number) {
    this.router.navigate([], {replaceUrl: true, queryParamsHandling: 'merge', queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: resultId}});
  }

  nextActive() {
    this._activatedQueue.next();
    this.router.navigate([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: this.query.getActiveId()}
    });
  }

  previousActive() {
    this._activatedQueue.previous();
    this.router.navigate([], {
      replaceUrl: true,
      queryParamsHandling: 'merge',
      queryParams: {[SENTINEL_SEARCH_ACTIVE_ID]: this.query.getActiveId()}
    });
  }

  clearResults() {
    this.store.set([]);
    this._clearSentinelSearchFromQuery();
  }

  connectQueryToActiveModal(): Observable<any> {
    return of(null)
      .pipe(
        merge(this.routerQuery.selectQueryParams(SENTINEL_SEARCH_ACTIVE_ID)),
        pairwise(),
        filter(([prevResultId, curResultId]) => curResultId != null && prevResultId == null),
        map(([prevResultId, curResultId]) => {
          try {
            return parseInt(curResultId);
          } catch (e) {
            return null;
          }
        }),
        filterNotNull(),
        switchMap(id => this.query.selectEntity(id).pipe(filterNotNull(), take(1))),
        tap(entity => {
          this.store.setActive(entity.id);
          this.modalService.show(makeDetailsModal());
        })
      );
  }

  connectQueryToForm(form: FormGroup): Observable<any> {
    return this.routerQuery.selectQueryParams([SENTINEL_SEARCH_PARAMS_QUERY_KEY, SENTINEL_PAGE_INDEX_QUERY_KEY])
      .pipe(
        take(1),
        map(([params, page]) => {
          try {
            return [JSON.parse(params as string), parseInt((page || '0') as string)];
          } catch (e) {
            return [{}, 0];
          }
        }),
        tap(([params, page]) => Object.keys(form.controls).forEach(key => form.get(key).patchValue(params[key] || {}))),
        switchMap(([params, page]) => this.query.selectShowSearchResults().pipe(take(1)).pipe(map(show => [show, params, page]))),
        filter(([show, params, page]) => show),
        switchMap(([show, params, page]) => this.search(form, page))
      );
  }

  redirectToLoginPage() {
    this.notificationService.addGeneral({type: 'warning', duration: 10000, content: 'Zaloguj się by uzyskać dostęp do zasobu'});
    this.router.navigate(['/login'], {queryParams: {[BACK_LINK_QUERY_PARAM]: this.routerQuery.getValue().state.url}});
  }

  setHovered(resultId: string | number | null) {
    this.store.update({hoveredId: resultId});
  }

  private _clearSentinelSearchFromQuery(): Promise<any> {
    return this.router.navigate([], {queryParamsHandling: 'merge', queryParams: {[SENTINEL_SHOW_RESULTS_QUERY_KEY]: undefined}});
  }

  setFootprint(footprint: string | null) {
    this.store.update({footprint});
  }
}
