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

import {IRemoteConfiguration} from './app.configuration';
import {Provider} from '@angular/core';
import {RemoteConfiguration} from './utils/initializer/config.service';
import {of} from 'rxjs';
import * as Factory from 'factory.ts';
import {LocalStorage} from './app.providers';
import {HashMap} from '@datorama/akita';

export const RemoteConfigurationFactory = Factory.makeFactory<IRemoteConfiguration>({
  osmUrl: '/osm/{z}/{x}/{y}.png',
  geoserverUrl: 'http://localhost:8080/geoserver/wms',
  geoserverWorkspace: 'testing',
  recaptchaSiteKey: 'abc123'
});

export const RemoteConfigurationTestingProvider: Provider = {
  provide: RemoteConfiguration,
  useValue: {
    get: () => RemoteConfigurationFactory.build(),
    isInitialized$: of(false)
  }
};

class LocalStorageMock {
  private store: HashMap<string>;
  constructor(initState: HashMap<string> = {}) {
    this.store = initState;
  }

  clear() {
    this.store = {};
  }

  getItem(key: string) {
    return this.store[key] || null;
  }

  setItem(key: string, value: string) {
    this.store[key] = value.toString();
  }

  removeItem(key: string) {
    delete this.store[key];
  }
}

export const LocalStorageTestingProvider: Provider = {
  provide: LocalStorage,
  useValue: new LocalStorageMock()
};

export function makeLocalStorageTestingProvider(
  initState: HashMap<string>
): Provider {
  return {
    provide: LocalStorage,
    useValue: new LocalStorageMock(initState)
  };
}
