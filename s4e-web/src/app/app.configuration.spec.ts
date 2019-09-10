import {Provider} from '@angular/core';
import {S4E_CONFIG} from './utils/initializer/config.service';

export const TestingConfigProvider: Provider = {
  provide: S4E_CONFIG, useValue: {
    geoserverUrl: 'http://localhost:8080/geoserver/wms',
    geoserverWorkspace: 'testing',
    backendDateFormat: 'yyyy-MM-dd\'T\'HH:mm:ss',
    recaptchaSiteKey: 'abc123'
  }
};
