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

import {InstitutionsSearchResultsStore} from './institutions-search-results.store';
import {InstitutionsSearchResultsQuery} from './institutions-search-results.query';
import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {InstitutionsSearchResultsService} from './institutions-search-results.service';
import {AkitaGuidService} from 'src/app/views/map-view/state/search-results/guid.service';
import {InstitutionQuery} from '../institution/institution.query';
import {of} from 'rxjs';

describe('InstitutionsSearchResultsService', () => {
  let institutionSearchResultsService: InstitutionsSearchResultsService;
  let institutionSearchResultsStore: InstitutionsSearchResultsStore;
  let institutionSearchResultsQuery: InstitutionsSearchResultsQuery;
  let institutionQuery: InstitutionQuery;
  let http: HttpTestingController;
  let guidService: AkitaGuidService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        InstitutionsSearchResultsService,
        InstitutionsSearchResultsQuery,
        InstitutionsSearchResultsStore,
        AkitaGuidService
      ],
      imports: [HttpClientTestingModule, RouterTestingModule]
    });

    http = TestBed.inject(HttpTestingController);
    institutionSearchResultsService = TestBed.inject(
      InstitutionsSearchResultsService
    );
    institutionSearchResultsStore = TestBed.inject(InstitutionsSearchResultsStore);
    institutionSearchResultsQuery = TestBed.inject(InstitutionsSearchResultsQuery);
    institutionQuery = TestBed.inject(InstitutionQuery);
    guidService = TestBed.inject(AkitaGuidService);
  });

  it('should be created', () => {
    expect(institutionSearchResultsService).toBeDefined();
  });

  describe('get', () => {
    it('should correctly handle empty query string', () => {
      institutionSearchResultsService.get('');

      expect(institutionSearchResultsQuery.getValue().isOpen).toEqual(true);
      expect(institutionSearchResultsQuery.getValue().queryString).toEqual('');
    });

    it('should handle non-empty query', fakeAsync(() => {
      const sampleResult = {
        id: '1234-5678',
        name: 'zarządzaj#1',
        slug: 'zarządzaj-1'
      };
      spyOn(institutionQuery, 'selectAll').and.returnValue(of([sampleResult]));
      institutionSearchResultsService.get('zarz');
      tick(1000);

      expect(institutionSearchResultsQuery.getValue().isOpen).toEqual(true);
      expect(institutionSearchResultsQuery.getValue().queryString).toEqual('zarz');
      expect(institutionSearchResultsQuery.getAll()).toEqual([sampleResult]);
    }));
  });
});
