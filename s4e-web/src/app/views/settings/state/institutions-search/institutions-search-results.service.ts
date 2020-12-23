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

import {InstitutionQuery} from './../institution/institution.query';
import {InstitutionService} from './../institution/institution.service';
import {InstitutionsSearchResultsStore} from './institutions-search-results.store';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AkitaGuidService} from 'src/app/views/map-view/state/search-results/guid.service';
import {Institution} from '../institution/institution.model';

@Injectable({providedIn: 'root'})
export class InstitutionsSearchResultsService {

  constructor(private _store: InstitutionsSearchResultsStore,
              private _guidGenerationService: AkitaGuidService,
              private _institutionService: InstitutionService,
              private _InstitutionQuery: InstitutionQuery,
              private _http: HttpClient) {
  }

  get(partialInstitutionName: string) {
    this._store.update({isOpen: true, queryString: partialInstitutionName});

    this._store.setLoading(true);
    this._InstitutionQuery.selectAll()
      .subscribe(institutions => this._store.set(
        institutions.filter((institution) => institution.name
          .toLocaleLowerCase()
          .indexOf(partialInstitutionName.toLocaleLowerCase()) > -1
        )
        )
      );
  }

  setSelectedInstitution(searchResult: Institution | null) {
    this._store.update({searchResult: searchResult, isOpen: false});

    if (!!searchResult) {
      this._institutionService.setActive(searchResult.slug);
    }
  }
}
