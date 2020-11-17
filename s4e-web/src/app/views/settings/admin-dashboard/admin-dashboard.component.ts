import { InstitutionsSearchResultsQuery } from './../state/institutions-search/institutions-search-results.query';
import { InstitutionsSearchResultsService } from './../state/institutions-search/institutions-search-results.service';
import { Router, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import {Component, OnDestroy, OnInit} from '@angular/core';
import { Institution } from '../state/institution/institution.model';
import { environment } from 'src/environments/environment';
import { InstitutionsSearchResultsStore } from '../state/institutions-search/institutions-search-results.store';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {filter, skip} from 'rxjs/operators';

@Component({
  selector: 's4e-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  public isInstitutionActive$ = this.institutionsSearchResultsQuery
    .isAnyInstitutionActive$(this._activatedRoute);
  public searchValue: string = '';

  constructor(
    private _institutionsSearchResultsService: InstitutionsSearchResultsService,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,

    public institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    public institutionSearchResultsStore: InstitutionsSearchResultsStore
  ) {}

  ngOnInit() {
    this.isInstitutionActive$
      .pipe(
        untilDestroyed(this),
        filter(isActive => !isActive),
        skip(1)
      )
      .subscribe(() => this.selectInstitution(null));

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
      ['/settings/institution'],
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
