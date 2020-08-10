import { IRemoteConfiguration } from './app.configuration';
import {Provider} from '@angular/core';
import { RemoteConfiguration } from './utils/initializer/config.service';
import { of } from 'rxjs';
import * as Factory from 'factory.ts';

export const RemoteConfigurationFactory = Factory.makeFactory<IRemoteConfiguration>({
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
