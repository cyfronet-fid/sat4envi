import { environment } from './../../../../../environments/environment';
import { httpGetRequest$, httpPostRequest$, httpPutRequest$ } from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PersonStore} from './person.store';
import {Observable} from 'rxjs';
import {DEFAULT_GROUP_SLUG, Person } from './person.model';
import {catchErrorAndHandleStore} from '../../../../common/store.util';
import {InstitutionQuery} from '../../state/institution/institution.query';

@Injectable({providedIn: 'root'})
export class PersonService {

  constructor(
    private store: PersonStore,
    private _institutionQuery: InstitutionQuery,
    private http: HttpClient
  ) {}

  fetchAll(institutionSlug: string) {
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/groups/${DEFAULT_GROUP_SLUG}/members`;
    httpGetRequest$<Person[]>(this.http, url, this.store)
      .subscribe(data => this.store.set(data));
  }

  deleteMember(userId: string) {
    const institutionSlug = this._institutionQuery.getActiveId();
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/groups/${DEFAULT_GROUP_SLUG}/members/${userId}`;
    httpPostRequest$(this.http, url, {}, this.store)
      .subscribe(() => this.store.remove(userId));
  }
}
