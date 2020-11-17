import { map } from 'rxjs/operators';
import { InstitutionQuery } from './../state/institution/institution.query';
import { Component, OnInit } from '@angular/core';
import {SessionQuery} from '../../../state/session/session.query';
import {Observable} from 'rxjs';
import { Institution } from '../state/institution/institution.model';

@Component({
  selector: 's4e-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {
  public MAX_ADMINISTRATION_INSTITUTIONS_TO_DISPLAY = 10;

  public userEmail$: Observable<string> = this._sessionQuery.select(state => state.email);
  public userName$: Observable<string> = this._sessionQuery.select(state => state.name);
  public userSurname$: Observable<string> = this._sessionQuery.select(state => state.surname);

  public administrationInstitutions$: Observable<Institution[]> = this._institutionQuery
    .selectAdministrationInstitutions$();
  public hasMoreAdministrationInstitutionsThanMax$ = this.administrationInstitutions$
    .pipe(map(institutions => institutions.length > this.MAX_ADMINISTRATION_INSTITUTIONS_TO_DISPLAY));

  public memberInstitutions$: Observable<Institution[]> = this._institutionQuery
    .selectMemberInstitutions$();

  constructor(
    private _sessionQuery: SessionQuery,
    private _institutionQuery: InstitutionQuery
  ) {}
}
