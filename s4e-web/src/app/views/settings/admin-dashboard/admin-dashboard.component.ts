import { InstitutionService } from './../state/institution/institution.service';
import { InstitutionsSearchResultsQuery } from './../state/institutions-search/institutions-search-results.query';
import { InstitutionsSearchResultsService } from './../state/institutions-search/institutions-search-results.service';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { Component, OnInit, OnDestroy, ViewChild, HostListener } from '@angular/core';
import { Institution } from '../state/institution/institution.model';
import { environment } from 'src/environments/environment';
import { map } from 'rxjs/operators';
import { InstitutionsSearchResultsStore } from '../state/institutions-search/institutions-search-results.store';

@Component({
  selector: 's4e-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {
  public hasSelectedInstitution$: Observable<boolean>;
  public searchValue: string = '';

  constructor(
    private _institutionsSearchResultsService: InstitutionsSearchResultsService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,

    public institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    public institutionSearchResultsStore: InstitutionsSearchResultsStore
  ) {}

  ngOnInit() {
    this.hasSelectedInstitution$ = this.institutionsSearchResultsQuery
      .hasInstitutionSlugIn$(this._activatedRoute);

    if (environment.hmr) {
      const searchResult = this.institutionsSearchResultsQuery.getValue().searchResult;
      this.searchValue = !!searchResult ? searchResult.name : '';
    }
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
}
