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

import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { SentinelSearchStore} from './sentinel-search.store';
import {
  SENTINEL_PAGE_INDEX_QUERY_KEY,
  SENTINEL_SELECTED_QUERY_KEY,
  SENTINEL_VISIBLE_QUERY_KEY,
  SentinelSearchResult,
  SentinelSearchState,
} from './sentinel-search.model';
import {combineLatest, Observable} from 'rxjs';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {filter, map} from 'rxjs/operators';
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

  selectHovered() {
    return this.select('hoveredId').pipe(map(id => this.getEntity(id)))
  }

  selectIsActiveFirst() {
    return combineLatest(
      this.selectAll(),
      this.selectActive()
    ).pipe(
      map(([results, active]) => results.indexOf(active) === 0)
    );
  }

  selectIsActiveLast() {
    return combineLatest(
      this.selectAll(),
      this.selectActive()
    ).pipe(
      map(([results, active]) => results.indexOf(active) === results.length - 1)
    );
  }

  selectShowSearchResults() {
    return this._routerQuery.selectQueryParams('showSearchResults').pipe(map(showSearchResults => showSearchResults === '1'))
  }

  selectCurrentPage() {
    return this._routerQuery.selectQueryParams(SENTINEL_PAGE_INDEX_QUERY_KEY)
      .pipe(map(page => {
        try {
          return parseInt(page as string);
        } catch(e) {
          return 0;
        }
      }));
  }
}
