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

import {handleHttpRequest$} from 'src/app/common/store.util';
import {
  ERROR_INTERCEPTOR_CODES_TO_SKIP,
  ERROR_INTERCEPTOR_SKIP_HEADER
} from '../../utils/error-interceptor/error.helper';
import {SessionStore} from './session.store';
import {action, resetStores} from '@datorama/akita';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import {COOKIE_POLICY_ACCEPTED_KEY, LoginFormState, Session} from './session.model';
import {
  HTTP_401_UNAUTHORIZED,
  HTTP_403_FORBIDDEN,
  HTTP_404_NOT_FOUND
} from '../../errors/errors.model';
import {EMPTY, Observable, of, throwError} from 'rxjs';
import {Router} from '@angular/router';
import environment from 'src/environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {NotificationService} from '../../notifications/state/notification.service';
import {CookieService} from 'ngx-cookie-service';

export const BACK_LINK_QUERY_PARAM = 'back_link';

@Injectable({providedIn: 'root'})
export class ProfileLoaderService {
  constructor(private _store: SessionStore, private _http: HttpClient) {}

  loadProfile$(): Observable<Session | null> {
    const url = `${environment.apiPrefixV1}/users/me`;
    const skipUnauthorizedHeader = {
      headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_401_UNAUTHORIZED.toString()}
    };
    return this._http.get<Session>(url, skipUnauthorizedHeader).pipe(
      tap(profile => this._store.update({...profile})),
      catchError(() => of(null))
    );
  }
}

@Injectable({providedIn: 'root'})
export class SessionService {
  private _backLink: string;

  constructor(
    private _store: SessionStore,
    private _notificationService: NotificationService,
    private _http: HttpClient,
    private _router: Router,
    private _profileLoaderService: ProfileLoaderService,
    private cookieService: CookieService
  ) {}

  loadProfile$() {
    return this._profileLoaderService.loadProfile$();
  }

  setBackLink(backLink: string) {
    this._backLink = backLink;
  }

  changePassword(oldPassword: string, newPassword: string) {
    const url = `${environment.apiPrefixV1}/password-change`;
    return this._http.post(url, {oldPassword, newPassword}).pipe(
      handleHttpRequest$(this._store),
      tap(() =>
        this._notificationService.addGeneral({
          type: 'success',
          content: 'Hasło zostało zmienione'
        })
      )
    );
  }

  sendPasswordResetToken$(email: string) {
    const url = `${environment.apiPrefixV1}/token-create?email=${email}`;
    return this._http.post(url, {}).pipe(
      tap(() =>
        this._notificationService.addGeneral({
          type: 'success',
          content: 'Link do resetu hasła został wysłany na podany adres email'
        })
      )
    );
  }

  resetPassword$(token: string, password: string) {
    const headers = {
      headers: {
        [ERROR_INTERCEPTOR_CODES_TO_SKIP]: [
          HTTP_401_UNAUTHORIZED.toString(),
          HTTP_404_NOT_FOUND.toString()
        ].join(',')
      }
    };
    const url = `${environment.apiPrefixV1}/password-reset`;
    return this._http.post(url, {token, password}, headers).pipe(
      catchError((error: HttpErrorResponse) => {
        switch (error.status) {
          case HTTP_404_NOT_FOUND:
            this._notificationService.addGeneral({
              type: 'error',
              content: 'Token dla resetu hasła nie istnieje'
            });
            break;
          case HTTP_401_UNAUTHORIZED:
            this._notificationService.addGeneral({
              type: 'error',
              content: 'Token resetu hasła przedawnił się'
            });
            break;
        }
        return EMPTY;
      }),
      tap(() =>
        this._notificationService.addGeneral({
          type: 'success',
          content: 'Hasło zostało zresetowane'
        })
      )
    );
  }

  @action('login')
  login$(request: LoginFormState) {
    const url = `${environment.apiPrefixV1}/login`;
    const headers = {
      headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_401_UNAUTHORIZED.toString()}
    };
    return this._http.post<LoginFormState>(url, request, headers).pipe(
      switchMap(data => this._profileLoaderService.loadProfile$()),
      tap(() => this._store.update({email: request.email})),
      catchError(error => {
        this._notificationService.addGeneral({
          content: 'Użytkownik nie istnieje, bądź hasło jest niepoprawne',
          type: 'error'
        });
        return throwError(error);
      })
    );
  }

  @action('logout')
  logout() {
    this._http
      .post(`${environment.apiPrefixV1}/logout`, {})
      .pipe(
        tap(() => resetStores()),
        tap(() => this._store.reset())
      )
      .subscribe(() => this._router.navigate(['/login']));
  }

  removeAccount$(email: string, password: string) {
    const url = `${environment.apiPrefixV1}/users/forget-me`;

    const headers = {
      headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_403_FORBIDDEN.toString()}
    };
    return this._http
      .post(url, {email, password}, headers)
      .pipe(handleHttpRequest$(this._store));
  }

  getJwtToken$(request: LoginFormState) {
    const url = `${environment.apiPrefixV1}/token`;
    const headers = {
      headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_401_UNAUTHORIZED.toString()}
    };
    return this._http
      .post<{email: string; token: string}>(url, request, headers)
      .pipe(map(response => response.token));
  }

  clearError() {
    this._store.setError(null);
  }

  goToLastUrl() {
    !!this._backLink && this._backLink !== ''
      ? this._router.navigateByUrl(this._backLink)
      : this._router.navigate(['/map/products']);

    delete this._backLink;
  }

  acceptCookiePolicy() {
    this._store.update({cookiePolicyAccepted: true});
    this.cookieService.set(COOKIE_POLICY_ACCEPTED_KEY, '1', new Date('2038-01-01'));
  }
}
