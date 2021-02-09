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

import {Injectable} from '@angular/core';
import {action} from '@datorama/akita';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {ActivateStore} from './activate.store';
import {Router} from '@angular/router';
import {delay, finalize} from 'rxjs/operators';
import environment from 'src/environments/environment';

@Injectable({providedIn: 'root'})
export class ActivateService {
  constructor(
    private _activateStore: ActivateStore,
    private _router: Router,
    private _http: HttpClient
  ) {}

  @action('activate')
  activate(token: string) {
    this._activateStore.setError(null);
    this._activateStore.setLoading(true);
    this._activateStore.setState('activating');

    this._http
      .post(`${environment.apiPrefixV1}/confirm-email`, {}, {params: {token}})
      .pipe(
        delay(1000),
        finalize(() => this._activateStore.setLoading(false))
      )
      .subscribe(
        () => {
          this._router.navigate(['/login']);
        },
        (error: HttpErrorResponse) => {
          this._activateStore.setError(error);
        }
      );
  }

  @action('resendToken')
  resendToken(token: string) {
    this._activateStore.setError(null);
    this._activateStore.setLoading(true);
    this._activateStore.setState('resending');

    this._http
      .post(
        `${environment.apiPrefixV1}/resend-registration-token-by-token`,
        {},
        {params: {token}}
      )
      .pipe(
        delay(1000),
        finalize(() => this._activateStore.setLoading(false))
      )
      .subscribe(
        () => {},
        (error: HttpErrorResponse) => {
          this._activateStore.setError(error);
        }
      );
  }
}
