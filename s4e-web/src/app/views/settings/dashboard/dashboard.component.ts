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

import {ActivatedRoute, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {InstitutionsSearchResultsQuery} from '../state/institutions-search/institutions-search-results.query';
import {Institution} from '../state/institution/institution.model';
import {InstitutionQuery} from '../state/institution/institution.query';
import {map} from 'rxjs/operators';

@Component({
  selector: 's4e-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  public isInstitutionActive$: Observable<boolean> = this._institutionsSearchResultsQuery.isAnyInstitutionActive$(
    this._activatedRoute
  );
  public activeInstitution$: Observable<Institution> = this._institutionQuery
    .selectAll()
    .pipe(map(institutions => institutions[0]));
  public isManagerOfActive$ = this._institutionQuery.isManagerOf$(
    this.activeInstitution$
  );

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute,
    private _institutionQuery: InstitutionQuery,
    private _router: Router
  ) {}
}
