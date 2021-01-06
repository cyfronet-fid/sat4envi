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

import { SessionService } from './../../../state/session/session.service';
import { environment } from './../../../../environments/environment';
import { handleHttpRequest$ } from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {RegisterStore} from './register.store';
import {tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import { RegisterFormState } from './register.model';
import {tokenize} from '@angular/compiler/src/ml_parser/lexer';

@Injectable({providedIn: 'root'})
export class RegisterService {

  constructor(
    private _store: RegisterStore,
    private _sessionService: SessionService,
    private _router: Router,
    private _http: HttpClient
  ) {}

  register(request: Partial<RegisterFormState>, recaptcha: string, token?: string) {
    const captchaQueryParam = `g-recaptcha-response=${recaptcha}`;
    let url = `${environment.apiPrefixV1}/register?${captchaQueryParam}`;

    if (!!token) {
      const tokenQueryParam = `token=${token}`;
      url = `${url}&${tokenQueryParam}`;
    }

    this._http.post<RegisterFormState>(url, {...request})
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._router.navigateByUrl('/'))
      )
      .subscribe(
        () => this._router.navigateByUrl('/register-confirmation'),
        errorResponse => {
          const errorMessage = errorResponse.status === 400
            ? errorResponse.error
            : {__general__: [errorResponse.error]};
          this._store.setError(errorMessage);
        }
      );
  }
}
