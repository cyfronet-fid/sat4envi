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

import {TestBed} from '@angular/core/testing';
import {IsManagerGuard} from './is-manager.guard';
import {Component} from '@angular/core';
import {Router, RouterModule} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import {SessionQuery} from '../../../../state/session/session.query';
import {SessionStore} from '../../../../state/session/session.store';

@Component({selector: 'neutral', template: ''})
class NeutralComponent {}

@Component({selector: 'restricted', template: ''})
class RestrictedComponent {}

describe('IsManagerGuard', () => {
  let router: Router;
  let query: SessionQuery;
  let store: SessionStore;
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NeutralComponent, RestrictedComponent],
      imports: [
        RouterModule.forRoot(
          [
            {path: 'settings/profile', component: NeutralComponent},
            {
              path: 'restricted',
              canActivate: [IsManagerGuard],
              component: RestrictedComponent
            }
          ],
          {useHash: true}
        )
      ],
      providers: [IsManagerGuard, SessionQuery, SessionStore]
    });
    router = TestBed.inject(Router);
    store = TestBed.inject(SessionStore);
    query = TestBed.inject(SessionQuery);
  });

  it('should create', () => {
    expect(TestBed.inject(IsManagerGuard)).toBeTruthy();
  });

  it('should allow if selectCanSeeInstitutions resolves true', async () => {
    spyOn(query, 'selectCanSeeInstitutions').and.returnValue(of(true));
    expect(await router.navigate(['/settings/profile'])).toBeTruthy();
    expect(router.isActive('settings/profile', true));
  });

  it('should return redirect if selectCanSeeInstitutions resolves false', async () => {
    spyOn(query, 'selectCanSeeInstitutions').and.returnValue(of(false));
    await router.navigate(['/restricted']);
    expect(router.isActive('/settings/profile', true)).toBeTruthy();
  });
});
