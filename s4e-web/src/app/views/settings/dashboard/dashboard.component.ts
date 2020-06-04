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
    private _instutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this.hasSelectedInstitution$ = this._instutionsSearchResultsQuery.hasSelectedInstitutionBy$(this._activatedRoute);
  }
}
