import {handleHttpRequest$} from 'src/app/common/store.util';
import {environment} from '../../../../../environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SentinelSearchStore} from './sentinel-search.store';
import {
  createSentinelSearchResult, SENTINEL_SEARCH_PARAMS_QUERY_KEY,
  SENTINEL_SELECTED_QUERY_KEY, SENTINEL_SHOW_RESULTS_QUERY_KEY,
  SENTINEL_VISIBLE_QUERY_KEY,
  SentinelSearchResultResponse,
} from './sentinel-search.model';
import {SentinelSearchQuery} from './sentinel-search.query';
import {catchError, delay, map, switchMap, take, tap} from 'rxjs/operators';
import {SentinelSearchMetadata} from './sentinel-search.metadata.model';
import {Router} from '@angular/router';
import {HashMap} from '@datorama/akita';
import {NotificationService} from 'notifications';
import {ModalService} from '../../../../modal/state/modal.service';
import {SENTINEL_SEARCH_RESULT_MODAL_ID} from '../../sentinel-search/search-result-modal/search-result-modal.model';
import {ActivatedQueue} from 'src/app/utils/search/activated-queue.utils';
import {Observable, of, throwError} from 'rxjs';
import {fromPromise} from 'rxjs/internal-compatibility';
import {FormGroup} from '@angular/forms';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {filterTrue} from '../../../../utils/rxjs/observable';
import {BACK_LINK_QUERY_PARAM} from '../../../../state/session/session.service';

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

  search(form: FormGroup): Observable<SentinelSearchResultResponse[]> {
    const params: HashMap<any> = Object.values(form.value).reduce((prev, current) => Object.assign(prev, current), {});

    this.store.set([]);
    this.store.update({loaded: false, loading: true, error: null});
    const url = `${environment.apiPrefixV1}/search`;
    return fromPromise(this.router.navigate([], {
      queryParams: {[SENTINEL_SEARCH_PARAMS_QUERY_KEY]: JSON.stringify(form.value), [SENTINEL_SHOW_RESULTS_QUERY_KEY]: '1'},
      queryParamsHandling: 'merge'
    }))
      .pipe(
        switchMap(() => this.http.get<SentinelSearchResultResponse[]>(url, {params})),
        handleHttpRequest$(this.store),
        delay(250),
        map(data => data.map(product => createSentinelSearchResult(product))),
        tap(data => {
          this.store.set(data);
          this.store.setLoaded(true);
        }),
        catchError(error => {
          this.notificationService.addGeneral({
            type: 'error',
            content: 'Wystąpił błąd podczas wyszukiwaniania'
          });
          return throwError(error);
        })
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

  openModalForResult(resultId: string) {
    this.store.setActive(resultId);
    this.modalService.show({id: SENTINEL_SEARCH_RESULT_MODAL_ID, size: 'lg'});
  }

  nextActive() {
    this._activatedQueue.next();
  }

  previousActive() {
    this._activatedQueue.previous();
  }

  clearResults() {
    this.store.set([]);
    this._clearSentinelSearchFromQuery();
  }

  connectQueryToForm(form: FormGroup): Observable<any> {
    return this.routerQuery.selectQueryParams(SENTINEL_SEARCH_PARAMS_QUERY_KEY).pipe(
      take(1),
      map((params: string) => {
        try {
          return JSON.parse(params);
        } catch (e) {
          return {};
        }
      }),
      tap(params => Object.keys(form.controls).forEach(key => form.get(key).patchValue(params[key] || {}))),
      switchMap(() => this.query.selectShowSearchResults().pipe(take(1))),
      filterTrue(),
      switchMap(() => this.search(form))
    );
  }

  private _clearSentinelSearchFromQuery(): Promise<any> {
    return this.router.navigate([], {queryParamsHandling: 'merge', queryParams: {[SENTINEL_SHOW_RESULTS_QUERY_KEY]: undefined}});
  }

  redirectToLoginPage() {
    this.notificationService.addGeneral({type: 'warning', duration: 10000, content: 'Zaloguj się by uzyskać dostęp do zasobu'})
    this.router.navigate(['/login'], {queryParams: {[BACK_LINK_QUERY_PARAM]: this.routerQuery.getValue().state.url}})
  }

  setHovered(resultId: string | number | null) {
    this.store.setLoading(true);
    this.store.update({hoveredId: resultId});
    this.store.setLoading(false);
  }
}
