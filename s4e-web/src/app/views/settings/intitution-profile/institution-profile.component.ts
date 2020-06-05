import { Institution } from './../state/institution/institution.model';
import { ActivatedRoute } from '@angular/router';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { InstitutionsSearchResultsQuery } from '../state/institutions-search/institutions-search-results.query';
import { untilDestroyed } from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-institution-profile',
  templateUrl: './institution-profile.component.html',
  styleUrls: ['./institution-profile.component.scss']
})
export class InstitutionProfileComponent implements OnInit, OnDestroy {
  public activeInstitution: Institution | null;

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this._institutionsSearchResultsQuery
      .getSelectedInstitutionBy$(this._activatedRoute)
      .pipe(untilDestroyed(this))
      .subscribe(institution => this.activeInstitution = institution);
  }

  ngOnDestroy() {}
}
