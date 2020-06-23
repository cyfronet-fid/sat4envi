import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { SentinelSearchStore} from './sentinel-search.store';
import {
  SENTINEL_SELECTED_QUERY_KEY,
  SENTINEL_VISIBLE_QUERY_KEY,
  SentinelSearchResult,
  SentinelSearchState,
} from './sentinel-search.model';
import {Observable} from 'rxjs';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {map} from 'rxjs/operators';
import {SentinelSearchMetadata} from './sentinel-search.metadata.model';

@Injectable({
  providedIn: 'root'
})
export class SentinelSearchQuery extends QueryEntity<SentinelSearchState, SentinelSearchResult> {
  selectSelectedSentinels(): Observable<string[]> {
    return this._routerQuery.selectQueryParams<string>(SENTINEL_SELECTED_QUERY_KEY)
      .pipe(map((sentinels => (sentinels || '').split(',').filter(str => str.length > 0))));
  }

  getSelectedSentinels(): string[] {
    return (this._routerQuery.getQueryParams<string>(SENTINEL_SELECTED_QUERY_KEY) || '').split(',').filter(str => str.length > 0);
  }

  selectVisibleSentinels(): Observable<string[]> {
    return this._routerQuery.selectQueryParams<string>(SENTINEL_VISIBLE_QUERY_KEY)
      .pipe(map((sentinels => (sentinels || '').split(',').filter(str => str.length > 0))));
  }

  getVisibleSentinels(): string[] {
    return (this._routerQuery.getQueryParams<string>(SENTINEL_VISIBLE_QUERY_KEY) || '').split(',').filter(str => str.length > 0);
  }

  constructor(protected store: SentinelSearchStore, private _routerQuery: RouterQuery) {
    super(store);
  }

  selectMetadataLoading(): Observable<boolean> {
    return this.select('metadataLoading');
  }

  selectMetadataLoaded(): Observable<boolean> {
    return this.select('metadataLoaded');
  }

  selectLoaded(): Observable<boolean> {
    return this.select('loaded');
  }

  isMetadataLoading(): boolean {
    return this.getValue().metadataLoading;
  }

  selectSentinels(): Observable<SentinelSearchMetadata> {
    return this.select('metadata');
  }

  isMetadataLoaded(): boolean {
    return this.getValue().metadataLoaded;
  }

  isSentinelSelected(sentinelId: string): boolean {
    return this.getSelectedSentinels().indexOf(sentinelId) >= 0;
  }

  isSentinelVisible(sentinelId: string): boolean {
    return this.getVisibleSentinels().indexOf(sentinelId) >= 0;
  }
}
