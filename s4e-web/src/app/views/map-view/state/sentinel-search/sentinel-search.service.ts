import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SentinelSearchStore} from './sentinel-search.store';
import {
  createSentinelSearchResult,
  SENTINEL_SELECTED_QUERY_KEY,
  SENTINEL_VISIBLE_QUERY_KEY,
  SentinelSearchResultResponse,
} from './sentinel-search.model';
import {SentinelSearchQuery} from './sentinel-search.query';
import {delay, finalize, map, shareReplay} from 'rxjs/operators';
import {catchErrorAndHandleStore} from '../../../../common/store.util';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {SentinelSearchMetadata} from './sentinel-search.metadata.model';
import {Router} from '@angular/router';
import {HashMap} from '@datorama/akita';
import {NotificationService} from 'notifications';

@Injectable({providedIn: 'root'})
export class SentinelSearchService {

  constructor(private store: SentinelSearchStore,
              private query: SentinelSearchQuery,
              private http: HttpClient,
              private router: Router,
              private notificationService: NotificationService,
              private CONFIG: S4eConfig) {
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

  search(params: HashMap<string>) {
    this.store.set([]);
    this.store.setLoading(true);
    const r = this.http.get<SentinelSearchResultResponse[]>(
      `${this.CONFIG.apiPrefixV1}/search`, {params}
      ).pipe(
        delay(250),
        catchErrorAndHandleStore(this.store),
        map(data => data.map(product => createSentinelSearchResult(product, this.CONFIG.apiPrefixV1))),
        finalize(() => this.store.setLoading(false)),
        shareReplay(1)
      );

    r.subscribe(
      data => {
        this.store.set(data);
        this.store.setLoaded(true);
      },
      error => this.notificationService.addGeneral({
        type: 'error',
        content: 'Wystąpił błąd podczas wyszukiwaniania'
      })
    );

    return r;
  }

  getSentinels() {
    if (this.query.isMetadataLoaded()) {
      return;
    }

    this.store.setMetadataLoading();

    const r = this.http.get<SentinelSearchMetadata>(`${this.CONFIG.apiPrefixV1}/config/sentinel-search`)
      .pipe(
        catchErrorAndHandleStore(this.store),
        finalize(() => this.store.setMetadataLoading(false)),
        shareReplay(1)
      );

    r.subscribe(
      data => this.store.update({metadata: data, metadataLoaded: true}),
      error => this.notificationService.addGeneral({
        type: 'error',
        content: 'Wystąpił błąd podczas pobierania metadanych'
      })
    );

    return r;
  }

  setLoaded(loaded: boolean) {
    this.store.setLoaded(loaded);
  }
}
