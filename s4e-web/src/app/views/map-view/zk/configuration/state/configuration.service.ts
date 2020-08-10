import { httpPostRequest$ } from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ConfigurationStore} from './configuration.store';
import {Configuration, ConfigurationState, ShareConfigurationRequest} from './configuration.model';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';
import {ConfigurationQuery} from './configuration.query';
import {Dao} from '../../../../../common/dao.service';
import {NotificationService} from 'notifications';
import {Observable, of} from 'rxjs';
import environment from 'src/environments/environment';

@Injectable({providedIn: 'root'})
export class ConfigurationService extends Dao<Configuration, ConfigurationState, ConfigurationStore> {
  constructor(store: ConfigurationStore,
              http: HttpClient,
              private notifications: NotificationService,
              private query: ConfigurationQuery) {
    super(http, `${environment.apiPrefixV1}/configurations`, store);
  }

  shareConfiguration(conf: ShareConfigurationRequest): Observable<boolean> {
    const url = `${environment.apiPrefixV1}/share-link`;
    const postRequest = httpPostRequest$(this.http, url, conf, this.store)
      .pipe(tap(() => this.notifications.addGeneral({
        type: 'success',
        content: `Link został wysłany na adres ${conf.emails.join(', ')}`
      })));

    postRequest.subscribe();
    return postRequest;
  }
}
