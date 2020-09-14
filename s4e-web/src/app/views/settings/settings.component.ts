import { InstitutionQuery } from './state/institution/institution.query';
import { InstitutionsSearchResultsStore } from './state/institutions-search/institutions-search-results.store';
import {InstitutionService} from './state/institution/institution.service';
import { Component, OnInit, OnDestroy } from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {InstitutionsSearchResultsQuery} from './state/institutions-search/institutions-search-results.query';
import {InstitutionsSearchResultsService} from './state/institutions-search/institutions-search-results.service';
import {ActivatedRoute, Router} from '@angular/router';
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
  public hasBeenSelected = false;

  public showInstitutions$: Observable<boolean> = this._sessionQuery
    .selectCanSeeInstitutions();
  public isInstitutionActive$: Observable<boolean> = this.institutionsSearchResultsQuery
    .isAnyInstitutionActive$(this._activatedRoute);

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
    if (environment.hmr) {
      const searchResult = this.institutionsSearchResultsQuery.getValue().searchResult;
      this.searchValue = !!searchResult ? searchResult.name : '';
    }
    this.institutionsSearchResultsQuery
      .selectActive$(this._activatedRoute)
      .pipe(untilDestroyed(this))
      .subscribe(institution => {
        const isSearchValueActiveInstitution = !!this.searchValue
          && this.searchValue === institution.name;
        if (!isSearchValueActiveInstitution) {
          this.searchValue = institution.name;
          this.hasBeenSelected = false;
        }
      });

    this._institutionService.get();
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
