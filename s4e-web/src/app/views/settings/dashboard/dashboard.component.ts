import {ActivatedRoute, Router} from '@angular/router';
import { Observable } from 'rxjs';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { InstitutionsSearchResultsQuery } from '../state/institutions-search/institutions-search-results.query';
import { Institution } from '../state/institution/institution.model';
import { InstitutionQuery } from '../state/institution/institution.query';
import {map} from 'rxjs/operators';

@Component({
  selector: 's4e-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  public isInstitutionActive$: Observable<boolean> = this._institutionsSearchResultsQuery
    .isAnyInstitutionActive$(this._activatedRoute);
  public activeInstitution$: Observable<Institution> = this._institutionQuery.selectAll()
    .pipe(map(institutions => institutions[0]));
  public isManagerOfActive$ = this._institutionQuery
    .isManagerOf$(this.activeInstitution$);

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute,
    private _institutionQuery: InstitutionQuery,
    private _router: Router
  ) {}
}
