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

import { handleHttpRequest$ } from 'src/app/common/store.util';

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ConfigurationStore} from './configuration.store';
import {Configuration, ConfigurationState, ShareConfigurationRequest} from './configuration.model';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';
import {ConfigurationQuery} from './configuration.query';
import {NotificationService} from 'notifications';
import {Observable, of} from 'rxjs';
import environment from 'src/environments/environment';

@Injectable({providedIn: 'root'})
export class ConfigurationService {
  constructor(
    private _store: ConfigurationStore,
    private _http: HttpClient,
    private _notificationsService: NotificationService,
    private _query: ConfigurationQuery
  ) {}

  shareConfiguration(conf: ShareConfigurationRequest): Observable<boolean> {
    const notificationContent = `Link został wysłany na adres ${conf.emails.join(', ')}`;
    const url = `${environment.apiPrefixV1}/share-link`;
    return this._http.post<ShareConfigurationRequest>(url, conf)
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._notificationsService.addGeneral({
          type: 'success',
          content: notificationContent
        })),
        map(() => true)
      );
  }
}
