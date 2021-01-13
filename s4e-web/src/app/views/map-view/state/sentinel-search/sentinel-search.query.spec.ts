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

import {SentinelSearchQuery} from './sentinel-search.query';
import {SentinelSearchStore} from './sentinel-search.store';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {createSentinelSearchResult, SENTINEL_SELECTED_QUERY_KEY, SENTINEL_VISIBLE_QUERY_KEY} from './sentinel-search.model';
import {ReplaySubject, Subject} from 'rxjs';
import {take, toArray} from 'rxjs/operators';
import {SentinelSearchFactory, SentinelSearchMetadataFactory} from './sentinel-search.factory.spec';

describe('SentinelSearchResultQuery', () => {
  let query: SentinelSearchQuery;
  let store: SentinelSearchStore;
  let routerQuery: RouterQuery;
  let queryParams$: Subject<string>;
  let selectQueryParamsSpy;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    query = TestBed.get(SentinelSearchQuery);
    store = TestBed.get(SentinelSearchStore);
    routerQuery = TestBed.get(RouterQuery);
    queryParams$ = new ReplaySubject<string>(1);
    selectQueryParamsSpy = spyOn(routerQuery, 'selectQueryParams').and.returnValue(queryParams$);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('getSelectedSentinels', () => {
    const spy = spyOn(routerQuery, 'getQueryParams').and.returnValue('sentinel1,sentinel2');
    expect(query.getSelectedSentinels()).toEqual(['sentinel1', 'sentinel2']);
    expect(spy).toHaveBeenCalledWith(SENTINEL_SELECTED_QUERY_KEY);
  });

  it('selectSelectedSentinels', async () => {
    queryParams$.next('sentinel1,sentinel2');
    expect(
      await query.selectSelectedSentinels().pipe(take(1)).toPromise()
    ).toEqual(['sentinel1', 'sentinel2']);

    expect(routerQuery.selectQueryParams).toHaveBeenCalledWith(SENTINEL_SELECTED_QUERY_KEY);
  });

  it('getVisibleSentinels', () => {
    const spy = spyOn(routerQuery, 'getQueryParams').and.returnValue('sentinel1,sentinel2');
    expect(query.getVisibleSentinels()).toEqual(['sentinel1', 'sentinel2']);
    expect(spy).toHaveBeenCalledWith(SENTINEL_VISIBLE_QUERY_KEY);
  });

  it('selectVisibleSentinels', async () => {
    queryParams$.next('sentinel1,sentinel2');
    expect(
      await query.selectVisibleSentinels().pipe(take(1)).toPromise()
    ).toEqual(['sentinel1', 'sentinel2']);

    expect(routerQuery.selectQueryParams).toHaveBeenCalledWith(SENTINEL_VISIBLE_QUERY_KEY);
  });

  it('selectMetadataLoading', async () => {
    expect(
      await query.selectMetadataLoading().pipe(take(1)).toPromise()
    ).toBe(false);

    store.setMetadataLoading(true);

    expect(
      await query.selectMetadataLoading().pipe(take(1)).toPromise()
    ).toBe(true);
  });

  it('selectMetadataLoaded', async () => {
    expect(
      await query.selectMetadataLoaded().pipe(take(1)).toPromise()
    ).toBe(false);

    store.setMetadataLoaded(true);

    expect(
      await query.selectMetadataLoaded().pipe(take(1)).toPromise()
    ).toBe(true);
  });

  it('isMetadataLoaded', () => {
    expect(query.isMetadataLoaded()).toBeFalsy();
    store.setMetadataLoaded(true);
    expect(query.isMetadataLoaded()).toBeTruthy();
  });

  it('selectLoaded', async () => {
    expect(
      await query.selectLoaded().pipe(take(1)).toPromise()
    ).toBe(false);

    store.setLoaded(true);

    expect(
      await query.selectLoaded().pipe(take(1)).toPromise()
    ).toBe(true);
  });

  it('selectSentinels', async () => {
    const metadata = SentinelSearchMetadataFactory.build();
    store.update({metadata});

    expect(
      await query.selectSentinels().pipe(take(1)).toPromise()
    ).toEqual(metadata);
  });

  it('isSentinelSelected', () => {
    const spy = spyOn(query, 'getSelectedSentinels').and.returnValue(['sentinel1', 'sentinel2']);
    expect(query.isSentinelSelected('sentinel1')).toBeTruthy();
    expect(query.isSentinelSelected('sentinel3')).toBeFalsy();
  });

  it('isSentinelVisible', () => {
    const spy = spyOn(query, 'getVisibleSentinels').and.returnValue(['sentinel1', 'sentinel2']);
    expect(query.isSentinelVisible('sentinel1')).toBeTruthy();
    expect(query.isSentinelVisible('sentinel3')).toBeFalsy();
  });

  it('selectIsActiveFirst', async () => {
    const results = SentinelSearchFactory
      .buildList(3)
      .map(result => createSentinelSearchResult(result));
    store.set(results);
    store.setActive(results[1].id);
    expect(await query.selectIsActiveFirst().pipe(take(1)).toPromise()).toBeFalsy();
    store.setActive(results[0].id);
    expect(await query.selectIsActiveFirst().pipe(take(1)).toPromise()).toBeTruthy();
  });

  it('selectIsActiveLast', async () => {
    const results = SentinelSearchFactory
      .buildList(3)
      .map(result => createSentinelSearchResult(result));
    store.set(results);
    store.setActive(results[1].id);
    expect(await query.selectIsActiveLast().pipe(take(1)).toPromise()).toBeFalsy();
    store.setActive(results[2].id);
    expect(await query.selectIsActiveLast().pipe(take(1)).toPromise()).toBeTruthy();
  });

  it('selectShowSearchResults', async () => {
    const params$ = new ReplaySubject(2);
    const promise = query.selectShowSearchResults().pipe(take(2), toArray()).toPromise()
    queryParams$.next(undefined);
    queryParams$.next('1');
    expect(await promise).toEqual([false, true]);
    expect(selectQueryParamsSpy).toHaveBeenCalledWith('showSearchResults');
  });
});
