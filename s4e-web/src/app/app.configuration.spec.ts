import { IRemoteConfiguration } from './app.configuration';
import {Provider} from '@angular/core';
import { RemoteConfiguration } from './utils/initializer/config.service';
import { of } from 'rxjs';
import * as Factory from 'factory.ts';
import {LocalStorage} from './app.providers';
import {HashMap} from '@datorama/akita';

export const RemoteConfigurationFactory = Factory.makeFactory<IRemoteConfiguration>({
  osmUrl: '/osm/{z}/{x}/{y}.png',
  geoserverUrl: 'http://localhost:8080/geoserver/wms',
  geoserverWorkspace: 'testing',
  recaptchaSiteKey: 'abc123',
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

export function makeLocalStorageTestingProvider(initState: HashMap<string>): Provider {
  return {
    provide: LocalStorage,
    useValue: new LocalStorageMock(initState)
  };
}
