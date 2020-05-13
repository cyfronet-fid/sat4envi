import {Injectable} from '@angular/core';
import {IConfiguration, IRemoteConfiguration} from '../../app.configuration';
import {map, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {combineLatest} from 'rxjs';
import {ProfileService} from '../../state/profile/profile.service';

@Injectable()
export class S4eConfig implements IConfiguration {

  backendDateFormat: string;
  geoserverUrl: string;
  geoserverWorkspace: string;
  recaptchaSiteKey: string;
  timezone: string;

  projection: { toProjection: string, coordinates: [number, number] };
  apiPrefixV1: string;
  userLocalStorageKey: string;
  generalErrorKey: string;
  maxZoom: number;

  constructor(private http: HttpClient, private profileService: ProfileService) {
  }

  init(config: IRemoteConfiguration) {
    this.geoserverUrl = config.geoserverUrl;
    this.geoserverWorkspace = config.geoserverWorkspace;
    this.recaptchaSiteKey = config.recaptchaSiteKey;

    this.backendDateFormat = 'YYYY-MM-DDTHH:mm:ssZ';
    this.projection = {toProjection: 'EPSG:3857', coordinates: [19, 52]};
    this.apiPrefixV1 = 'api/v1';
    this.userLocalStorageKey = 'user';
    this.generalErrorKey = '__general__';
    this.maxZoom = 12;
    this.timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
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
