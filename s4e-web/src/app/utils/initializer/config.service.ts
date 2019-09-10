import {Injectable, InjectionToken, Provider} from '@angular/core';
import {IConfiguration} from '../../app.configuration';
import {InitService} from './init.service';

@Injectable()
export class S4eConfig implements IConfiguration {

  backendDateFormat: string;
  geoserverUrl: string;
  geoserverWorkspace: string;
  recaptchaSiteKey: string;

  constructor(private initService: InitService) {
    const config = initService.getConfiguration();

    this.backendDateFormat = config.backendDateFormat;
    this.geoserverUrl = config.geoserverUrl;
    this.geoserverWorkspace = config.geoserverWorkspace;
    this.recaptchaSiteKey = config.recaptchaSiteKey;
  }
}

export const S4E_CONFIG = new InjectionToken<IConfiguration>('S4E_CONFIG');
export const ConfigProvider: Provider = {provide: S4E_CONFIG, useClass: S4eConfig};

