import {Injectable, InjectionToken, Provider} from '@angular/core';
import {IConfiguration, IRemoteConfiguration} from '../../app.configuration';
import {map, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {combineLatest, of} from 'rxjs';
import {ProfileService} from '../../state/profile/profile.service';

@Injectable()
export class S4eConfig implements IConfiguration {

  backendDateFormat: string;
  geoserverUrl: string;
  geoserverWorkspace: string;
  recaptchaSiteKey: string;
  momentDateFormat: string;
  momentDateFormatShort: string;

  projection: { toProjection: string, coordinates: [number, number] };
  apiPrefixV1: string;
  userLocalStorageKey: string;
  generalErrorKey: string;

  constructor(private http: HttpClient, private profileService: ProfileService) {
  }

  init(config: IRemoteConfiguration) {
    this.geoserverUrl = config.geoserverUrl;
    this.geoserverWorkspace = config.geoserverWorkspace;
    this.recaptchaSiteKey = config.recaptchaSiteKey;

    this.backendDateFormat = 'YYYY-MM-DDTHH:mm:ssZ';
    this.momentDateFormat = 'YYYY-MM-DDTHH:mm:ss[Z]';
    this.momentDateFormatShort = 'YYYY-MM-DD';
    this.projection = {toProjection: 'EPSG:3857', coordinates: [19, 52]};
    this.apiPrefixV1 = 'api/v1';
    this.userLocalStorageKey = 'user';
    this.generalErrorKey = '__general__';
  }

  loadConfiguration(): Promise<IRemoteConfiguration> {
    return combineLatest([
      this.http.get<IRemoteConfiguration>(`api/v1/config`).pipe(tap(config => this.init(config))),
      this.profileService.get$()
    ]).pipe(
      map(([configuration, profile]) => configuration)
    ).toPromise();
  }
}
