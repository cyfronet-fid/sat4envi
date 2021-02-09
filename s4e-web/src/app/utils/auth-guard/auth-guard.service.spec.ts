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
import {IsLoggedIn, IsNotLoggedIn} from './auth-guard.service';
import {RouterTestingModule} from '@angular/router/testing';
import {SessionQuery} from '../../state/session/session.query';
import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {HttpClientTestingModule} from '@angular/common/http/testing';

@Component({selector: 'neutral', template: ''})
class NeutralComponent {}

@Component({selector: 'sub', template: ''})
class SubComponent {}

@Component({selector: 'login', template: ''})
class LoginComponent {}

describe('IsLoggedIn & IsNotLoggedIn', () => {
  let router: Router;
  let query: SessionQuery;
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SubComponent, LoginComponent, NeutralComponent],
      imports: [
        RouterTestingModule.withRoutes([
          {path: 'map/products', component: NeutralComponent},
          {path: 'login', canActivate: [IsNotLoggedIn], component: LoginComponent},
          {path: 'sub', canActivate: [IsLoggedIn], component: SubComponent}
        ]),
        HttpClientTestingModule
      ],
      providers: [IsLoggedIn, IsNotLoggedIn, SessionQuery]
    });
    router = TestBed.inject(Router);
    query = TestBed.inject(SessionQuery);
  });
  describe('IsLoggedIn', () => {
    it('should create', () => {
      expect(TestBed.inject(IsLoggedIn)).toBeTruthy();
    });

    it('should let in if user is logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(true);
      await router.navigate(['/sub']);
      expect(router.isActive('/sub', true)).toBeTruthy();
    });

    it('should redirect to root if user is not logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(false);
      await router.navigate(['/sub']);
      expect(router.isActive('/login', true)).toBeTruthy();
    });
  });

  describe('IsNotLoggedIn', () => {
    it('should create', () => {
      expect(TestBed.inject(IsNotLoggedIn)).toBeTruthy();
    });

    it('should redirect to root if user is logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(true);
      await router.navigate(['/login']);
      expect(router.isActive('/map/products', true)).toBeTruthy();
    });

    it('should let user if he/she is not logged in', async () => {
      spyOn(query, 'isLoggedIn').and.returnValue(false);
      expect(await router.navigate(['/login'])).toBeTruthy();
    });
  });
});
