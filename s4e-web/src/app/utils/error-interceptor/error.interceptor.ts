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

import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError, filter, finalize, map, pairwise, tap} from 'rxjs/operators';
import {Router, RoutesRecognized} from '@angular/router';
import {
  HTTP_101_TIMEOUT,
  HTTP_401_UNAUTHORIZED,
  HTTP_403_FORBIDDEN,
  HTTP_404_BAD_REQUEST,
  HTTP_404_NOT_FOUND,
  HTTP_500_INTERNAL_SERVER_ERROR,
  HTTP_502_BAD_GATEWAY
} from '../../errors/errors.model';
import {HttpErrorHelper} from './error.helper';
import {NotificationService} from 'notifications';
import {BACK_LINK_QUERY_PARAM} from '../../state/session/session.service';
import {resetStores} from '@datorama/akita';


@Injectable({
  providedIn: 'root',
})
export class ErrorInterceptor implements HttpInterceptor {
  private _backLink: string;

  constructor(
    private _router: Router,
    private _notificationService: NotificationService
  ) {
    this._router.events
      .pipe(
        filter(event => event instanceof RoutesRecognized),
        pairwise(),
        map((event: [RoutesRecognized, RoutesRecognized]) => event[0].url)
      )
      .subscribe((lastUrl: string) => this._backLink = !!lastUrl ? '/' + lastUrl : null);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    request = HttpErrorHelper.addDefaultHeaders(request);

    return next.handle(request)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (HttpErrorHelper.hasMutuallyExclusiveOptions(request)) {
            console.error('HTTP request has mutually exclusive options, make sure that only one skip header is provided');
          } else if (!HttpErrorHelper.skipHandling(error, request)) {
            if (HttpErrorHelper.isClientSideError(error)) {
              // this is client side unexpected error, generally this should not happen
              console.error(error.message);
            } else if (HttpErrorHelper.isServerSideError(error)) {
              return throwError(error).pipe(finalize(() => this._handleServerError(error)));
            }
          }
          return throwError(error);
        })
      );
  }

  private _handleServerError = (error: HttpErrorResponse) => {
    switch (error.status) {
      case HTTP_403_FORBIDDEN:
      case HTTP_401_UNAUTHORIZED:
        resetStores();
        this._notificationService.addGeneral({
          type: 'error',
          content: 'Wystąpił błąd: Nie jesteś zalogowany lub twoja sesja się przedawniła'
        });
        this._router.navigate(
          ['login'],
          {
            queryParams: !!this._backLink
              ? {[BACK_LINK_QUERY_PARAM]: this._backLink}
              : {}
          }
        );
        break;
      case HTTP_101_TIMEOUT:
      case HTTP_404_BAD_REQUEST:
        this._notificationService.addGeneral({
          type: 'error',
          content: 'Wystąpił błąd: Nieprawidłowe zapytanie lub zbyt długi czas oczekiwania na odpowiedź serwera'
        });
        break;
      case HTTP_404_NOT_FOUND:
        this._router.navigate(['errors', error.status]);
        break;
      case HTTP_502_BAD_GATEWAY:
      case HTTP_500_INTERNAL_SERVER_ERROR:
        this._router.navigate(
          ['errors', error.status],
          {
            queryParams: !!this._backLink
              ? {[BACK_LINK_QUERY_PARAM]: this._backLink}
              : {}
          }
        );
        break;
      default:
        this._notificationService.addGeneral({
          type: 'error',
          content: `Wystąpił nieznany błąd: ${error.status}, w przypadku dalszego występowania skontaktuj się z administratorem`
        });
        break;
    }
  };
}
