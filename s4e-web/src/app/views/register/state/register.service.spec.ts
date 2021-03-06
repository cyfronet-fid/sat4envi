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

import {RegisterFactory} from '../register.factory.spec';
import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {RegisterService} from './register.service';
import {RegisterStore} from './register.store';
import {RouterTestingModule} from '@angular/router/testing';
import {RegisterQuery} from './register.query';
import {Router} from '@angular/router';
import environment from 'src/environments/environment';

describe('RegisterService', () => {
  let registerService: RegisterService;
  let registerStore: RegisterStore;
  let http: HttpTestingController;
  let registerQuery: RegisterQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RegisterService, RegisterStore, RegisterQuery],
      imports: [HttpClientTestingModule, RouterTestingModule]
    });

    http = TestBed.inject(HttpTestingController);
    registerService = TestBed.inject(RegisterService);
    registerStore = TestBed.inject(RegisterStore);
    registerQuery = TestBed.inject(RegisterQuery);
  });

  it('should be created', () => {
    expect(registerService).toBeDefined();
  });

  describe('register', () => {
    it('should correctly pass userRegister', () => {
      const userRegister = RegisterFactory.build();
      const {recaptcha, passwordRepeat, ...request} = userRegister;
      registerService.register(request, recaptcha);
      const req = http.expectOne(
        `${environment.apiPrefixV1}/register?g-recaptcha-response=${userRegister.recaptcha}`
      );
      expect(req.request.method).toBe('POST');

      expect(req.request.body).toEqual(request);
      req.flush({});
      http.verify();
    });

    it('should redirect on success', fakeAsync(() => {
      const router = TestBed.inject(Router);
      const spy = spyOn(router, 'navigateByUrl').and.stub();

      const userRegister = RegisterFactory.build();
      const {recaptcha, passwordRepeat, ...request} = userRegister;
      registerService.register(request, recaptcha);
      const req = http.expectOne(
        `${environment.apiPrefixV1}/register?g-recaptcha-response=${userRegister.recaptcha}`
      );
      req.flush({});

      tick(1000);
      http.verify();
      expect(spy).toBeCalledWith('/register-confirmation');
    }));

    it('should redirect on success with invite to institution', fakeAsync(() => {
      const router = TestBed.inject(Router);
      const spy = spyOn(router, 'navigateByUrl').and.stub();

      const userRegister = RegisterFactory.build();
      const {recaptcha, passwordRepeat, ...request} = userRegister;
      const token = 'ddddddddddddddd';
      registerService.register(request, recaptcha, token);
      const req = http.expectOne(
        `${environment.apiPrefixV1}/register?g-recaptcha-response=${userRegister.recaptcha}&token=${token}`
      );
      req.flush({});

      tick(1000);
      http.verify();
      expect(spy).toBeCalledWith('/register-confirmation');
    }));

    it('should handle error 400', done => {
      const userRegister = RegisterFactory.build();
      const {recaptcha, passwordRepeat, ...request} = userRegister;
      registerService.register(request, recaptcha);
      const req = http.expectOne(
        `${environment.apiPrefixV1}/register?g-recaptcha-response=${userRegister.recaptcha}`
      );
      req.flush(
        {email: ['Invalid email']},
        {status: 400, statusText: 'Bad Request'}
      );

      registerQuery.selectError().subscribe(error => {
        expect(error).toEqual({email: ['Invalid email']});
        done();
      });

      http.verify();
    });

    it('should handle other errors', done => {
      const userRegister = RegisterFactory.build();
      const {recaptcha, passwordRepeat, ...request} = userRegister;
      registerService.register(request, recaptcha);
      const req = http.expectOne(
        `${environment.apiPrefixV1}/register?g-recaptcha-response=${userRegister.recaptcha}`
      );
      req.flush('Server Failed', {status: 500, statusText: 'Server Error'});

      registerQuery.selectError().subscribe(error => {
        expect(error).toEqual({__general__: ['Server Failed']});
        done();
      });

      http.verify();
    });
  });
});
