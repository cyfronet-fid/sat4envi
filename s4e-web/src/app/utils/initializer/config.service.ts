import {Injectable, InjectionToken, Provider} from '@angular/core';
import {IConfiguration, IRemoteConfiguration, RemoteConfigurationResponse} from '../../app.configuration';
import {map, tap} from 'rxjs/operators';
import {deserializeJsonResponse} from '../miscellaneous/miscellaneous';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class S4eConfig implements IConfiguration {

  backendDateFormat: string;
  geoserverUrl: string;
  geoserverWorkspace: string;
  recaptchaSiteKey: string;

  projection: { toProjection: string, coordinates: [number, number] };
  apiPrefixV1: string;
  userLocalStorageKey: string;
  generalErrorKey: string;

  constructor(private http: HttpClient) {
  }

  init(config: IRemoteConfiguration) {
    this.backendDateFormat = config.backendDateFormat;
    this.geoserverUrl = config.geoserverUrl;
    this.geoserverWorkspace = config.geoserverWorkspace;
    this.recaptchaSiteKey = config.recaptchaSiteKey;

    this.projection = {toProjection: 'EPSG:3857', coordinates: [19, 52]};
    this.apiPrefixV1 = 'api/v1';
    this.userLocalStorageKey = 'user';
    this.generalErrorKey = '__general__';
  }

  loadConfiguration(): Promise<IRemoteConfiguration> {
    return this.http.get<IRemoteConfiguration>(`api/v1/config`).pipe(
      map(data => deserializeJsonResponse(data, RemoteConfigurationResponse)),
      tap(config => this.init(config))
    ).toPromise();
  }
}
