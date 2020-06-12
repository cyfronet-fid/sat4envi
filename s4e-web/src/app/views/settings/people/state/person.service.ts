import { httpGetRequest$, httpPostRequest$, httpPutRequest$ } from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PersonStore} from './person.store';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {Observable} from 'rxjs';
import {DEFAULT_GROUP_SLUG, Person, PersonForm} from './person.model';
import {catchErrorAndHandleStore} from '../../../../common/store.util';
import {InstitutionQuery} from '../../state/institution/institution.query';

@Injectable({providedIn: 'root'})
export class PersonService {

  constructor(private store: PersonStore,
              private _institutionQuery: InstitutionQuery,
              private http: HttpClient, private config: S4eConfig) {
  }

  fetchAll(institutionSlug: string) {
    const url = `${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${DEFAULT_GROUP_SLUG}/members`;
    httpGetRequest$<Person[]>(this.http, url, this.store)
      .subscribe(data => this.store.set(data));
  }

  create$(institutionSlug: string, value: PersonForm): Observable<boolean> {
    const groupSlugs = [...value.groupSlugs._, 'default'];
    const person = {...value, groupSlugs} as undefined as Person;
    const url = `${this.config.apiPrefixV1}/institutions/${institutionSlug}/users`;
    return httpPostRequest$(this.http, url, person, this.store);
  }

  update$(institutionSlug: string, value: PersonForm) {
    const groupSlugs = [...value.groupSlugs._, 'default'];
    const person = {...value, groupSlugs} as undefined as Person;
    const url = `${this.config.apiPrefixV1}/institutions/${institutionSlug}/users`;
    return httpPutRequest$(this.http, url, person, this.store);
  }

  deleteMember(userId: string) {
    const institutionSlug = this._institutionQuery.getActiveId();
    httpPostRequest$(this.http,
      `${this.config.apiPrefixV1}/institutions/${institutionSlug}/groups/${DEFAULT_GROUP_SLUG}/members/${userId}`, {}, this.store)
      .subscribe(
        () => this.store.remove(userId)
      );
  }
}
