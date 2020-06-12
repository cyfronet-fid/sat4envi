import { map } from 'rxjs/operators';
import { InstitutionService } from './../state/institution/institution.service';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { InstitutionsSearchResultsQuery } from '../state/institutions-search/institutions-search-results.query';

@Component({
  selector: 's4e-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  public hasSelectedInstitution$: Observable<boolean>;

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.hasSelectedInstitution$ = this._institutionsSearchResultsQuery
      .getSelectedInstitutionSlugBy$(this._activatedRoute)
      .pipe(map(slug => !!slug));
  }
}
