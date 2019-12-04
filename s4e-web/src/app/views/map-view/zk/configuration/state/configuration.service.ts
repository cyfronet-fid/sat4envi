import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ConfigurationStore} from './configuration.store';
import {Configuration, ConfigurationState, ShareConfigurationRequest} from './configuration.model';
import {catchError, finalize, map, shareReplay} from 'rxjs/operators';
import {ConfigurationQuery} from './configuration.query';
import {S4eConfig} from '../../../../../utils/initializer/config.service';
import {Dao} from '../../../../../common/dao.service';
import {Observable, of} from 'rxjs';

@Injectable({providedIn: 'root'})
export class ConfigurationService extends Dao<Configuration, ConfigurationState, ConfigurationStore> {
  constructor(store: ConfigurationStore,
              http: HttpClient,
              private config: S4eConfig,
              private query: ConfigurationQuery) {
    super(http, `${config.apiPrefixV1}/configurations`, store);
  }

  shareConfiguration(conf: ShareConfigurationRequest): Observable<boolean> {
    this.store.setLoading(true);
    const r = this.http.post<void>(`${this.config.apiPrefixV1}/share-link`, conf)
      .pipe(
        map(r => {
          return true;
        }), catchError(error => {
          this.store.setError(error);
          return of(false);
        }),
        finalize(() => this.store.setLoading(false)),
        shareReplay(1)
      );

    r.subscribe();
    return r;
  }
}
