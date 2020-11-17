import { InstitutionQuery } from './state/institution/institution.query';
import { InstitutionsSearchResultsStore } from './state/institutions-search/institutions-search-results.store';
import {InstitutionService} from './state/institution/institution.service';
import { Component, OnInit, OnDestroy } from '@angular/core';
import {Observable} from 'rxjs';
import {filter, finalize, map, switchMap} from 'rxjs/operators';
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
  public hasBeenSelected = false;

  public showInstitutions$: Observable<boolean> = this._sessionQuery
    .selectCanSeeInstitutions();
  public isInstitutionActive$: Observable<boolean> = this.institutionsSearchResultsQuery
    .isAnyInstitutionActive$(this._activatedRoute);
  public activeInstitution$: Observable<Institution> = this.institutionsSearchResultsQuery
    .selectActive$(this._activatedRoute);
  public isManagerOfActive$ = this._institutionQuery
    .isManagerOf$(this.activeInstitution$);

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

    this.isInstitutionActive$
      .pipe(
        untilDestroyed(this),
        filter(isActive => !isActive)
      )
      .subscribe(() => this.selectInstitution(null));

    this.isInstitutionActive$
      .pipe(
        untilDestroyed(this),
        filter(isActive => isActive),
        switchMap(() => this._institutionQuery.selectHasOnlyOneAdministrationInstitution()),
        switchMap(() => this._router.navigate(
        ['/settings/dashboard'],
        {
          relativeTo: this._activatedRoute,
          queryParams: {
            institution: this._institutionQuery.getAdministrationInstitutions()[0].slug
          },
          queryParamsHandling: 'merge'
        }))
      )
      .subscribe();
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
