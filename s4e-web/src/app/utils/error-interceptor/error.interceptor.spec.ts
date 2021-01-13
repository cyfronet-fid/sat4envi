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

import {RouterTestingModule} from '@angular/router/testing';
import {ErrorInterceptor} from './error.interceptor';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController, TestRequest} from '@angular/common/http/testing';
import {HTTP_INTERCEPTORS, HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {catchError} from 'rxjs/operators';
import {of} from 'rxjs';

describe('ErrorInterceptor', () => {
  let errorInterceptor: ErrorInterceptor;
  let httpController: HttpTestingController;
  let http: HttpClient;
  let router: Router;

  const url = `/api1/newTest`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        ErrorInterceptor,
        {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true}
      ]
    });
    router = TestBed.get(Router);
    spyOn(router, 'navigate').and.stub();

    errorInterceptor = TestBed.get(ErrorInterceptor);
    httpController = TestBed.get(HttpTestingController);
    http = TestBed.get(HttpClient);
  });

  it('Should handle client/server error', async () => {
    const r = http.get(url).pipe(catchError(error => of(error))).toPromise();
    const request: TestRequest = httpController.expectOne(url);
    request.flush({}, {status: 500, statusText: 'Server Error'});

    expect((await r).status).toBe(500);
    expect((await r).message).toBe(`Http failure response for ${url}: 500 Server Error`);

    httpController.verify();
    expect(router.navigate).toHaveBeenCalled();
  });
});
