/*
 * Copyright 2020 ACC Cyfronet AGH
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

import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { InstitutionStore, InstitutionState } from './institution.store';
import { Institution } from './institution.model';
import {filter, map} from 'rxjs/operators';
import { Observable } from 'rxjs';
import { SessionQuery } from 'src/app/state/session/session.query';

@Injectable({
  providedIn: 'root'
})
export class InstitutionQuery extends QueryEntity<InstitutionState, Institution> {
  constructor(
    protected store: InstitutionStore,
    private _sessionQuery: SessionQuery
  ) {
    super(store);
  }

  public selectHasOnlyOneAdministrationInstitution() {
    return this.selectAdministrationInstitutions$()
      .pipe(map(insitutions => insitutions.length === 1));
  }
  public selectAdministrationInstitutions$(): Observable<Institution[]> {
    return this.selectAll()
      .pipe(map(institutions => this._getAdministrationInstitutionsBy(institutions)));
  }
  public getAdministrationInstitutions(): Institution[] {
    return this._getAdministrationInstitutionsBy(this.getAll());
  }

  public selectMemberInstitutions$(): Observable<Institution[]> {
    return this.selectAll()
      .pipe(map(institutions => this._getMemberInstitutionsBy(institutions)));
  }
  public getMemberInstitutions(): Institution[] {
    return this._getMemberInstitutionsBy(this.getAll());
  }

  public isManagerOf$(institution$: Observable<Institution>): Observable<boolean> {
    return institution$
      .pipe(map(institution => this.isManagerOf(institution)));
  }
  public isManagerOf(institution: Institution): boolean {
    const administratorInstitutionsSlugs = this._sessionQuery.getAdministratorInstitutionsSlugs();
    return administratorInstitutionsSlugs.some(slug => slug === institution.slug);
  }


  private _getAdministrationInstitutionsBy(institutions: Institution[]): Institution[] {
    const administratorInstitutionsSlugs = this._sessionQuery.getAdministratorInstitutionsSlugs();
    return institutions
      .filter(institution => administratorInstitutionsSlugs.some(slug => slug === institution.slug));
  }
  private _getMemberInstitutionsBy(institutions: Institution[]): Institution[] {
    const memberInstitutionSlugs = this._sessionQuery.getMemberInstitutionSlugs();
    return institutions
      .filter(institution => memberInstitutionSlugs.some(slug => slug === institution.slug));
  }
}
