import {HttpClient} from '@angular/common/http';
import {PersonStore} from './person.store';
import {DEFAULT_GROUP_SLUG, Person } from './person.model';
import { InstitutionQuery } from '../../../state/institution/institution.query';
import environment from 'src/environments/environment';
import { handleHttpRequest$ } from 'src/app/common/store.util';
import { Injectable } from '@angular/core';

@Injectable({providedIn: 'root'})
export class PersonService {

  constructor(
    private _store: PersonStore,
    private _institutionQuery: InstitutionQuery,
    private _http: HttpClient
  ) {}

  fetchAll(institutionSlug: string) {
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/members`;
    this._http.get<Person[]>(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe((data) => this._store.set(data));
  }

  deleteMember(userId: string) {
    const institutionSlug = this._institutionQuery.getActiveId();
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/members/${userId}`;
    this._http.post(url, {})
      .pipe(handleHttpRequest$(this._store))
      .subscribe();
  }
}
