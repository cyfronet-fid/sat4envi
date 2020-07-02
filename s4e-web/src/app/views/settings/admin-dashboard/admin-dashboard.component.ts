import { InstitutionService } from './../state/institution/institution.service';
import { InstitutionsSearchResultsQuery } from './../state/institutions-search/institutions-search-results.query';
import { InstitutionsSearchResultsService } from './../state/institutions-search/institutions-search-results.service';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Institution } from '../state/institution/institution.model';
import { environment } from 'src/environments/environment';
import { map } from 'rxjs/operators';

@Component({
  selector: 's4e-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  public institutions$: Observable<Institution[]>;
  public institutionsLoading$: Observable<boolean>;
  public areResultsOpen$: Observable<boolean>;

  public hasSelectedInstitution$: Observable<boolean>;
  public searchValue: string = '';
  public isInUse: boolean = false;

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _institutionsSearchResultsService: InstitutionsSearchResultsService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.institutions$ = this._institutionsSearchResultsQuery.selectAll()
      .pipe(map(institutions => institutions.filter(institution => !!institution)));;
    this.institutionsLoading$ = this._institutionsSearchResultsQuery.selectLoading();
    this.areResultsOpen$ = this._institutionsSearchResultsQuery.selectIsOpen();

    this.hasSelectedInstitution$ = this._institutionsSearchResultsQuery
      .hasInstitutionSlugIn$(this._activatedRoute);

    if (environment.hmr) {
      const searchResult = this._institutionsSearchResultsQuery.getValue().searchResult;
      this.searchValue = !!searchResult ? searchResult.name : '';
    }
  }

  searchForInstitutions(partialInstitutionName: string) {
    this._institutionsSearchResultsService.get(partialInstitutionName || '');
    this.isInUse = true;
  }

  selectFirstInstitution() {
    const firstSearchResult = this._institutionsSearchResultsQuery.getAll()[0];
    if(!!firstSearchResult) {
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
