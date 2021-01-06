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

import {SessionQuery} from './session.query';
import {async, TestBed} from '@angular/core/testing';
import {CommonStateModule} from '../common-state.module';
import {take} from 'rxjs/operators';
import {SessionStore} from './session.store';
import {RoleFactory} from './session.factory.spec';

describe('SessionQuery', () => {
  let query: SessionQuery;
  let store: SessionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [CommonStateModule],
    });

    query = TestBed.get(SessionQuery);
    store = TestBed.get(SessionStore);
  }));

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('should selectMemberZK', async () => {
    store.update({memberZK: false});
    expect(await query.selectMemberZK().pipe(take(1)).toPromise()).toBe(false);
    store.update({memberZK: true});
    expect(await query.selectMemberZK().pipe(take(1)).toPromise()).toBe(true);
  });

  function canSeeInstitutions$(): Promise<boolean> {
    return query.selectCanSeeInstitutions().pipe(take(1)).toPromise();
  }

  it('should resolve to true if user is admin', async () => {
    store.update({admin: true});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has INST_ADMIN role in group', async () => {
    store.update({roles: [RoleFactory.build({role: 'INST_ADMIN'})]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has GROUP_MANAGER role in group', async () => {
    store.update({roles: [RoleFactory.build({role: 'GROUP_MANAGER'})]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to true if user has INST_MANAGER role in group', async () => {
    store.update({roles: [RoleFactory.build({role: 'INST_MANAGER'})]});
    expect(await canSeeInstitutions$()).toBeTruthy();
  });

  it('should resolve to false if user has no managerial role', async () => {
    store.update({roles: [RoleFactory.build({role: 'INST_MEMBER'})]});
    expect(await canSeeInstitutions$()).toBeFalsy();
  });

  it('should not resolve to true if user has at least one managerial role in group', async () => {
    store.update({
      roles: [
        RoleFactory.build({role: 'INST_MEMBER'}),
        RoleFactory.build({role: 'GROUP_MANAGER'})
      ]
    });
    expect(await canSeeInstitutions$()).toBeTruthy();
  });
});
