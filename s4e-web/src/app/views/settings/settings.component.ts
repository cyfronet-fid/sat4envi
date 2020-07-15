import {InstitutionService} from './state/institution/institution.service';
import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {InstitutionsSearchResultsQuery} from './state/institutions-search/institutions-search-results.query';
import {InstitutionsSearchResultsService} from './state/institutions-search/institutions-search-results.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Institution} from './state/institution/institution.model';
import {environment} from 'src/environments/environment';
import {SessionQuery} from '../../state/session/session.query';

@Component({
  selector: 's4e-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  showInstitutions$: Observable<boolean>;
  public institutions$: Observable<Institution[]>;
  public institutionsLoading$: Observable<boolean>;
  public areResultsOpen$: Observable<boolean>;

  public searchValue: string;
  public isInUse: boolean = false;

  public hasSelectedInstitution$: Observable<boolean>;

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _institutionsSearchResultsService: InstitutionsSearchResultsService,
    private _institutionService: InstitutionService,
    private _sessionQuery: SessionQuery,
    private _router: Router,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.showInstitutions$ = this._sessionQuery.selectCanSeeInstitutions();
    this.institutions$ = this._institutionsSearchResultsQuery.selectAll()
      .pipe(map(institutions => institutions.filter(institution => !!institution)));
    this.institutionsLoading$ = this._institutionsSearchResultsQuery.selectLoading();
    this.areResultsOpen$ = this._institutionsSearchResultsQuery.selectIsOpen();

    this.hasSelectedInstitution$ = this._institutionsSearchResultsQuery
      .hasInstitutionSlugIn$(this._activatedRoute);

    // TODO: set institution from storage
    if (environment.hmr) {
      const searchResult = this._institutionsSearchResultsQuery.getValue().searchResult;
      this.searchValue = !!searchResult ? searchResult.name : '';
    }

    this._institutionService.get();
  }

  searchForInstitutions(partialInstitutionName: string) {
    this._institutionsSearchResultsService.get(partialInstitutionName || '');
    this.isInUse = true;
  }

  selectFirstInstitution() {
    const firstSearchResult = this._institutionsSearchResultsQuery.getAll()[0];
    if (!!firstSearchResult) {
      this.selectInstitution(firstSearchResult);
    }
  }

  selectInstitution(institution: Institution | null) {
    this.searchValue = !!institution && institution.name || '';
    this.isInUse = false;
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

  resetSearch() {
    this.selectInstitution(null);
  }
}
