import { NotificationService } from 'notifications';
import {HttpClient} from '@angular/common/http';
import {PersonStore} from './person.store';
import {DEFAULT_GROUP_SLUG, Person, UserRole} from './person.model';
import { InstitutionQuery } from '../../../state/institution/institution.query';
import environment from 'src/environments/environment';
import { handleHttpRequest$ } from 'src/app/common/store.util';
import { Injectable } from '@angular/core';
import { map } from 'rxjs/operators';
import { Institution } from '../../../state/institution/institution.model';

@Injectable({providedIn: 'root'})
export class PersonService {

  constructor(
    private _store: PersonStore,
    private _institutionQuery: InstitutionQuery,
    private _http: HttpClient,
    private _notificationService: NotificationService
  ) {}

  fetchAll(institutionSlug: string) {
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/members`;
    this._http.get<Person[]>(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe((persons: Person[]) => this._store.set(persons));
  }

  addAdminRoleFor(person: Person) {
    const institutionSlug = this._institutionQuery.getActiveId();
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/admins/${person.id}`;
    this._http.post(url, {})
      .pipe(handleHttpRequest$(this._store))
      .subscribe(() => {
          const newRole: UserRole = {
            institutionSlug,
            role: 'INST_ADMIN'
          };
          this._store.update(person.email, {roles: [...person.roles, newRole]});
          this._notificationService.addGeneral({
          content: `${person.email} otrzymał rolę administratora`,
          type: 'success'
        });
      });
  }

  removeAdminRoleFor(person: Person) {
    const institutionSlug = this._institutionQuery.getActiveId();
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/admins/${person.id}`;
    this._http.delete(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe(() => {
        this._store.update(person.email, {roles: person.roles.filter(role => role.role !== 'INST_ADMIN' && role.institutionSlug !== institutionSlug)});
        this._notificationService.addGeneral({
          content: `Rola administratora dla ${person.email} została usunięta`,
          type: 'success'
        });
      });
  }

  delete(person: Person) {
    const institutionSlug = this._institutionQuery.getActiveId();
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/members/${person.id}`;
    this._http.post(url, {})
      .pipe(handleHttpRequest$(this._store))
      .subscribe(() => this._store.remove(person.email));
  }
}
