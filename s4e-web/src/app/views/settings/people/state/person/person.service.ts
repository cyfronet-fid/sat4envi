/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {HttpClient, HttpHeaders} from '@angular/common/http';
import {PersonStore} from './person.store';
import {Person, UserRole} from './person.model';
import {InstitutionQuery} from '../../../state/institution/institution.query';
import environment from 'src/environments/environment';
import {handleHttpRequest$} from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {map, switchMap, tap} from 'rxjs/operators';
import {forkJoin} from 'rxjs';
import {Institution} from '../../../state/institution/institution.model';
import {NotificationService} from '../../../../../notifications/state/notification.service';

@Injectable({providedIn: 'root'})
export class PersonService {
  constructor(
    private _store: PersonStore,
    private _institutionQuery: InstitutionQuery,
    private _http: HttpClient,
    private _notificationService: NotificationService
  ) {}

  fetchAllAdmins$() {
    this._store.set([]);
    const isAdmin = person => person.roles.some(role => role.role === 'INST_ADMIN');
    return this._institutionQuery.selectAll().pipe(
      switchMap(institutions =>
        forkJoin(
          institutions.map(institution =>
            this.fetchAllBy$(institution).pipe(
              handleHttpRequest$(this._store),
              map(persons => persons.filter(person => isAdmin(person)))
            )
          )
        )
      ),
      map(institutionsPersons =>
        institutionsPersons.reduce(
          (persons, institutionPersons) =>
            (persons = [...persons, ...institutionPersons])
        )
      ),
      map(nonUniquePersons => {
        const nonUniquePersonsEmails = nonUniquePersons.map(person => person.email);
        const uniquePersonsIndexes = Array.from(
          new Set(nonUniquePersonsEmails)
        ).map(uniqueEmail => nonUniquePersonsEmails.indexOf(uniqueEmail));

        return uniquePersonsIndexes.map(
          uniquePersonIndex => nonUniquePersons[uniquePersonIndex]
        );
      }),
      switchMap(persons => this._updateWithPrivileges(persons)),
      tap(persons => this._store.set(persons))
    );
  }

  fetchAllBy$(institution: Institution) {
    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/members`;
    return this._http.get<Person[]>(url).pipe(handleHttpRequest$(this._store));
  }

  fetchAll(institutionSlug: string) {
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/members`;
    this._http
      .get<Person[]>(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe((persons: Person[]) => this._store.set(persons));
  }

  addAdminRoleFor(person: Person) {
    const institutionSlug = this._institutionQuery.getActiveId();
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/admins/${person.id}`;
    this._http
      .post(url, {})
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
    this._http
      .delete(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe(() => {
        this._store.update(person.email, {
          roles: person.roles.filter(
            role =>
              role.role !== 'INST_ADMIN' && role.institutionSlug !== institutionSlug
          )
        });
        this._notificationService.addGeneral({
          content: `Rola administratora dla ${person.email} została usunięta`,
          type: 'success'
        });
      });
  }

  delete(person: Person) {
    const institutionSlug = this._institutionQuery.getActiveId();
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/members/${person.id}`;
    this._http
      .delete(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe(() => this._store.remove(person.email));
  }

  toggleDeletePrivilegeFor$(person: Person) {
    const url = `${environment.apiPrefixV1}/users/authority/`;
    const body = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: {email: person.email}
    };
    const removePrivilege$ = this._http
      .delete(url + 'OP_INSTITUTION_DELETE', body)
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => {
          this._store.replace(person.email, {
            ...person,
            hasGrantedDeleteInstitution: false
          });
        })
      );
    const addPrivilege$ = this._http
      .put(url + 'OP_INSTITUTION_DELETE', {email: person.email})
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => {
          this._store.replace(person.email, {
            ...person,
            hasGrantedDeleteInstitution: true
          });
        })
      );
    return person.hasGrantedDeleteInstitution ? removePrivilege$ : addPrivilege$;
  }

  private _updateWithPrivileges(persons: Person[]) {
    const url = `${environment.apiPrefixV1}/users/authority/OP_INSTITUTION_DELETE`;
    return this._http.get<{email: string}[]>(url).pipe(
      map(emails => emails.map(email => email.email)),
      map(emails =>
        persons.map(person => {
          person.hasGrantedDeleteInstitution = emails.some(
            email => email === person.email
          );
          return person;
        })
      )
    );
  }
}
