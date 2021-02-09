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

import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpRequest
} from '@angular/common/http';
import {Observable} from 'rxjs';
import {
  HTTP_502_BAD_GATEWAY,
  HTTP_404_BAD_REQUEST,
  HTTP_500_INTERNAL_SERVER_ERROR
} from '../../errors/errors.model';

export const ERROR_INTERCEPTOR_CODES_TO_SKIP = 'Error-Interceptor-Codes-To-Skip';
export const ERROR_INTERCEPTOR_CODES_TO_HANDLE = 'Error-Interceptor-Codes-To-Handle';
export const ERROR_INTERCEPTOR_SKIP_HEADER = 'Error-Interceptor-Skip-Header';
export const ERROR_INTERCEPTOR_HANDLE_ALL_HEADER =
  'Error-Interceptor-Handle-All-Header';

// this is an option which can be passed to http.get / http.post etc.
export const HANDLE_ALL_ERRORS = {
  headers: {
    [ERROR_INTERCEPTOR_HANDLE_ALL_HEADER]: ERROR_INTERCEPTOR_HANDLE_ALL_HEADER
  }
};

export const DEFAULT_SKIP_CODES = {
  POST: [
    HTTP_404_BAD_REQUEST.toString(),
    HTTP_500_INTERNAL_SERVER_ERROR.toString(),
    HTTP_502_BAD_GATEWAY.toString()
  ],
  PUT: [
    HTTP_404_BAD_REQUEST.toString(),
    HTTP_500_INTERNAL_SERVER_ERROR.toString(),
    HTTP_502_BAD_GATEWAY.toString()
  ]
};

export class HttpErrorHelper {
  public static isServerSideError(error: HttpErrorResponse): boolean {
    return error instanceof HttpErrorResponse;
  }

  public static isClientSideError(error: HttpErrorResponse): boolean {
    return error instanceof HttpErrorResponse && error.error instanceof ErrorEvent;
  }

  public static skipErrorCode(
    error: HttpErrorResponse,
    request: HttpRequest<any>
  ): boolean {
    return (
      request.headers.has(ERROR_INTERCEPTOR_CODES_TO_SKIP) &&
      request.headers.get(ERROR_INTERCEPTOR_CODES_TO_SKIP).length > 0 &&
      request.headers
        .get(ERROR_INTERCEPTOR_CODES_TO_SKIP)
        .split(',')
        .map(errorCode => +errorCode.trim())
        .includes(error.status)
    );
  }

  public static handleErrorCode(
    error: HttpErrorResponse,
    request: HttpRequest<any>
  ): boolean {
    return (
      request.headers.has(ERROR_INTERCEPTOR_HANDLE_ALL_HEADER) ||
      (request.headers.has(ERROR_INTERCEPTOR_CODES_TO_HANDLE) &&
        request.headers.get(ERROR_INTERCEPTOR_CODES_TO_HANDLE).length > 0 &&
        error.status in
          request.headers
            .get(ERROR_INTERCEPTOR_CODES_TO_HANDLE)
            .split(',')
            .map(errorCode => +errorCode)) ||
      !request.headers.has(ERROR_INTERCEPTOR_CODES_TO_HANDLE)
    );
  }

  public static hasSkipHeader(request: HttpRequest<any>): boolean {
    return request.headers.has(ERROR_INTERCEPTOR_SKIP_HEADER);
  }

  public static handledHttpEvent(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(
      request.clone({
        headers: request.headers
          .delete(ERROR_INTERCEPTOR_SKIP_HEADER)
          .delete(ERROR_INTERCEPTOR_CODES_TO_SKIP)
          .delete(ERROR_INTERCEPTOR_CODES_TO_HANDLE)
          .delete(ERROR_INTERCEPTOR_HANDLE_ALL_HEADER)
      })
    );
  }

  /**
   * Only one header amongst
   * (ERROR_INTERCEPTOR_CODES_TO_HANDLE,
   * ERROR_INTERCEPTOR_CODES_TO_SKIP,
   * ERROR_INTERCEPTOR_CODES_TO_HANDLE,
   * ERROR_INTERCEPTOR_HANDLE_ALL_HEADER)
   *
   * can be used
   *
   * @param request
   */
  static hasMutuallyExclusiveOptions(request: HttpRequest<any>) {
    let optionsCount = 0;
    if (request.headers.has(ERROR_INTERCEPTOR_CODES_TO_HANDLE)) {
      ++optionsCount;
    }
    if (request.headers.has(ERROR_INTERCEPTOR_CODES_TO_SKIP)) {
      ++optionsCount;
    }
    if (request.headers.has(ERROR_INTERCEPTOR_SKIP_HEADER)) {
      ++optionsCount;
    }
    if (request.headers.has(ERROR_INTERCEPTOR_HANDLE_ALL_HEADER)) {
      ++optionsCount;
    }

    return optionsCount > 1;
  }

  static skipHandling(error: HttpErrorResponse, request: HttpRequest<any>) {
    if (request.headers.has(ERROR_INTERCEPTOR_HANDLE_ALL_HEADER)) {
      return false;
    }
    if (HttpErrorHelper.hasSkipHeader(request)) {
      return true;
    }
    if (HttpErrorHelper.skipErrorCode(error, request)) {
      return true;
    }
    if (HttpErrorHelper.handleErrorCode(error, request)) {
      return false;
    }
    return false;
  }

  static hasAnyOption(request: HttpRequest<any>) {
    return (
      request.headers.has(ERROR_INTERCEPTOR_CODES_TO_HANDLE) ||
      request.headers.has(ERROR_INTERCEPTOR_CODES_TO_SKIP) ||
      request.headers.has(ERROR_INTERCEPTOR_SKIP_HEADER) ||
      request.headers.has(ERROR_INTERCEPTOR_HANDLE_ALL_HEADER)
    );
  }

  static addDefaultHeaders(request: HttpRequest<any>): HttpRequest<any> {
    if (!HttpErrorHelper.hasAnyOption(request)) {
      request = request.clone({
        headers: request.headers.append(
          ERROR_INTERCEPTOR_CODES_TO_SKIP,
          DEFAULT_SKIP_CODES[request.method] || []
        )
      });
    }
    return request;
  }
}
