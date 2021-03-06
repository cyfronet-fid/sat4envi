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

import {IRemoteConfiguration} from '../../app.configuration';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import environment from 'src/environments/environment';
import {Observable, ReplaySubject} from 'rxjs';
import {tap} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class RemoteConfiguration {
  private _isInitialized$ = new ReplaySubject<boolean>(1);

  private _configuration: IRemoteConfiguration;

  constructor() {
    this._isInitialized$.next(false);
  }

  getIsInitialized$(): Observable<boolean> {
    return this._isInitialized$.asObservable();
  }

  get(): IRemoteConfiguration {
    return this._configuration;
  }

  set(configuration: IRemoteConfiguration) {
    this._configuration = configuration;
    this._isInitialized$.next(true);
  }
}

@Injectable()
export class ConfigurationLoader {
  constructor(
    private _http: HttpClient,
    private _remoteConfiguration: RemoteConfiguration
  ) {}

  load$(): Promise<any> {
    const url = `${environment.apiPrefixV1}/config`;
    return this._http
      .get<IRemoteConfiguration>(url)
      .pipe(tap(configuration => this._remoteConfiguration.set(configuration)))
      .toPromise();
  }
}
