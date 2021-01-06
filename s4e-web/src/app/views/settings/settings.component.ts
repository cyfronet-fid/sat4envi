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

import { InstitutionQuery } from './state/institution/institution.query';
import { InstitutionsSearchResultsStore } from './state/institutions-search/institutions-search-results.store';
import {InstitutionService} from './state/institution/institution.service';
import { Component, OnInit, OnDestroy } from '@angular/core';
import {Observable} from 'rxjs';
import {filter, finalize, map, switchMap, tap} from 'rxjs/operators';
import {InstitutionsSearchResultsQuery} from './state/institutions-search/institutions-search-results.query';
import {InstitutionsSearchResultsService} from './state/institutions-search/institutions-search-results.service';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {Institution} from './state/institution/institution.model';
import {environment} from 'src/environments/environment';
import {SessionQuery} from '../../state/session/session.query';
import { untilDestroyed } from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit, OnDestroy {
  public isMobileSidebarOpen = false;
  public searchValue: string;

  public hasAnyAdminInstitution$ = this._institutionQuery
    .selectAdministrationInstitutions$()
    .pipe(filter(institutions => !!institutions && institutions.length > 0));
  public showInstitutions$: Observable<boolean> = this._sessionQuery
    .selectCanSeeInstitutions();
  public isInstitutionActive$: Observable<boolean> = this.institutionsSearchResultsQuery
    .isAnyInstitutionActive$(this._activatedRoute);
  public activeInstitution$: Observable<Institution> = this.institutionsSearchResultsQuery
    .selectActive$(this._activatedRoute);
  public isManagerOfActive$ = this._institutionQuery
    .isManagerOf$(this.activeInstitution$);
  public isAdminOfOneInstitution$ = this._institutionQuery
    .selectHasOnlyOneAdministrationInstitution();
  public isSuperAdmin$ = this._sessionQuery.select('admin');

  public canGrantInstitutionDeleteAuthority;

  constructor(
    private _institutionsSearchResultsService: InstitutionsSearchResultsService,
    private _institutionService: InstitutionService,
    private _institutionQuery: InstitutionQuery,
    private _sessionQuery: SessionQuery,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,

    public institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    public institutionsSearchResultsStore: InstitutionsSearchResultsStore
  ) {}

  ngOnInit() {
    // Set page scroll at initial place
    window.scrollTo(0, 0);
    this._router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map(event => (event as NavigationEnd).url),
        filter(url => url.indexOf('/settings') > -1),
        finalize(() => window.scrollTo(0, 0))
      )
      .pipe(untilDestroyed(this))
      .subscribe();

    this._institutionService.get();

    this.canGrantInstitutionDeleteAuthority = this._sessionQuery.canGrantInstitutionDeleteAuthority();
  }

  searchForInstitutions(partialInstitutionName: string) {
    if (!partialInstitutionName || partialInstitutionName === '') {
      this.selectInstitution(null);
    }

    this._institutionsSearchResultsService.get(partialInstitutionName || '');
  }

  selectInstitution(institution: Institution | null) {
    this.searchValue = !!institution && institution.name || '';
    this._institutionsSearchResultsService.setSelectedInstitution(institution);

    this._router.navigate(
      !!institution ? [] : ['/settings'],
      {
        relativeTo: this._activatedRoute,
        queryParams: {
          institution: !!institution && institution.slug || null
        },
        queryParamsHandling: 'merge'
      }
    );
  }

  ngOnDestroy() {}
}
