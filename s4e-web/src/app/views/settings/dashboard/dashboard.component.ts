import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { InstitutionsSearchResultsQuery } from '../state/institutions-search/institutions-search-results.query';

@Component({
  selector: 's4e-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  public isInstitutionActive$: Observable<boolean>;

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute
  ) {
    this.isInstitutionActive$ = this._institutionsSearchResultsQuery
      .isAnyInstitutionActive$(this._activatedRoute);
  }
}
