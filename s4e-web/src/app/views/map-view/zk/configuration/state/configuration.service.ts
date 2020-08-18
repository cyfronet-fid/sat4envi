import { handleHttpRequest$ } from 'src/app/common/store.util';

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ConfigurationStore} from './configuration.store';
import {Configuration, ConfigurationState, ShareConfigurationRequest} from './configuration.model';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';
import {ConfigurationQuery} from './configuration.query';
import {NotificationService} from 'notifications';
import {Observable, of} from 'rxjs';
import environment from 'src/environments/environment';

@Injectable({providedIn: 'root'})
export class ConfigurationService {
  constructor(
    private _store: ConfigurationStore,
    private _http: HttpClient,
    private _notificationsService: NotificationService,
    private _query: ConfigurationQuery
  ) {}

  shareConfiguration(conf: ShareConfigurationRequest): Observable<boolean> {
    const notificationContent = `Link został wysłany na adres ${conf.emails.join(', ')}`;
    const url = `${environment.apiPrefixV1}/share-link`;
    return this._http.post<ShareConfigurationRequest>(url, conf)
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._notificationsService.addGeneral({
          type: 'success',
          content: notificationContent
        })),
        map(() => true)
      );
  }
}
