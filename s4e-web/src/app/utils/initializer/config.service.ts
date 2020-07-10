import {Injectable} from '@angular/core';
import {IConfiguration, IRemoteConfiguration} from '../../app.configuration';
import {map, switchMap, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {SessionService} from '../../state/session/session.service';

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

  constructor(private http: HttpClient, private sessionService: SessionService) {
  }

  init(config: IRemoteConfiguration) {
    this.geoserverUrl = config.geoserverUrl;
    this.geoserverWorkspace = config.geoserverWorkspace;
    this.recaptchaSiteKey = config.recaptchaSiteKey;

    this.backendDateFormat = 'YYYY-MM-DDTHH:mm:ss[Z]';
    this.projection = {toProjection: 'EPSG:3857', coordinates: [19, 52]};
    this.apiPrefixV1 = 'api/v1';
    this.userLocalStorageKey = 'user';
    this.generalErrorKey = '__general__';
    this.maxZoom = 12;
    this.timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

    this.sessionService.init(this);
  }

  loadConfiguration(): Promise<S4eConfig> {
    return this.http.get<IRemoteConfiguration>(`api/v1/config`)
      .pipe(
        tap(config => this.init(config)),
        switchMap(() => this.sessionService.getProfile$()),
        map(() => this)
      ).toPromise();
  }
}
