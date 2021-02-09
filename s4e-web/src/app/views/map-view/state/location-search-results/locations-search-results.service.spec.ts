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

import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {SearchResultsService} from './locations-search-results.service';
import {LocationSearchResultsStore} from './locations-search-results.store';
import {RouterTestingModule} from '@angular/router/testing';
import {AkitaGuidService} from '../search-results/guid.service';
import {LocationSearchResultsQuery} from './location-search-results.query';
import environment from 'src/environments/environment';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';

describe('SearchResultsService', () => {
  let searchResultsService: SearchResultsService;
  let searchResultsStore: LocationSearchResultsStore;
  let searchResultsQuery: LocationSearchResultsQuery;
  let http: HttpTestingController;
  let guidService: AkitaGuidService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    http = TestBed.inject(HttpTestingController);
    searchResultsService = TestBed.inject(SearchResultsService);
    searchResultsStore = TestBed.inject(LocationSearchResultsStore);
    searchResultsQuery = TestBed.inject(LocationSearchResultsQuery);
    guidService = TestBed.inject(AkitaGuidService);
  });

  it('should be created', () => {
    expect(searchResultsService).toBeDefined();
  });

  describe('get', () => {
    it('should correctly handle empty query string', () => {
      searchResultsService.get('');

      expect(searchResultsQuery.getValue().isOpen).toEqual(false);
      expect(searchResultsQuery.getValue().queryString).toEqual('');

      http.verify();
    });

    it('should pass query to endpoint', () => {
      searchResultsService.get('foo');
      const req = http.expectOne(`${environment.apiPrefixV1}/places?namePrefix=foo`);
      expect(req.request.method).toBe('GET');
      req.flush({});
      http.verify();
    });

    it('should handle non-empty query', fakeAsync(() => {
      const sampleId = '1234-5678';
      jest.spyOn(guidService, 'guid').mockReturnValueOnce(sampleId);
      searchResultsService.get('k');
      const req = http.expectOne(`${environment.apiPrefixV1}/places?namePrefix=k`);
      expect(req.request.method).toBe('GET');

      const sampleResult = {
        latitude: 12,
        longitude: 23,
        name: 'kraków',
        type: 'miasto',
        voivodeship: 'małopolska'
      };
      req.flush({
        content: [sampleResult]
      });
      tick(1000);

      const expectedResult = {...sampleResult, id: sampleId};
      expect(searchResultsQuery.getValue().isOpen).toEqual(true);
      expect(searchResultsQuery.getValue().queryString).toEqual('k');
      expect(searchResultsQuery.getAll()).toEqual([expectedResult]);
    }));
  });
});
