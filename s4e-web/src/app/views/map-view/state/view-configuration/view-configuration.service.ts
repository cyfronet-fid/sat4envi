import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ViewConfigurationStore} from './view-configuration.store';
import {ViewConfiguration} from './view-configuration.model';
import {catchError, finalize, map, shareReplay} from 'rxjs/operators';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {Observable, throwError} from 'rxjs';
import {IPageableResponse} from '../../../../state/pagable.model';
import {ViewConfigurationQuery} from './view-configuration.query';

@Injectable({providedIn: 'root'})
export class ViewConfigurationService {

  constructor(private store: ViewConfigurationStore,
              private query: ViewConfigurationQuery,
              private config: S4eConfig,
              private http: HttpClient) {
  }

  add$(viewConfiguration: ViewConfiguration): Observable<boolean> {
    this.store.setError(null);
    this.store.setLoading(true);
    const r = this.http.post<ViewConfiguration>(`${this.config.apiPrefixV1}/saved-views`, viewConfiguration)
      .pipe(
        map(r => true),
        catchError(error => {
          this.store.setError(error);
          return throwError(error);
        }),
        finalize(() => this.store.setLoading(false)),
        shareReplay(1));
    r.subscribe();
    return r;
  }

  delete(uuid: string) {
    this.store.setError(null);
    this.store.setLoading(true);
    this.http.delete(`${this.config.apiPrefixV1}/saved-views/${uuid}`, {
      headers: {'Content-Type': 'application/json'}
    }).pipe(
      finalize(() => this.store.setLoading(false))
    ).subscribe(
      () => this.store.remove(uuid),
      error => this.store.setError(error)
    );
  }

  get() {
    this.store.setError(null);
    this.store.setLoading(true);
    this.http.get<IPageableResponse<ViewConfiguration>>(`${this.config.apiPrefixV1}/saved-views`, {
      headers: {'Content-Type': 'application/json'}
    }).pipe(
      finalize(() => this.store.setLoading(false))
    ).subscribe(
      data => this.store.set(data.content),
      error => this.store.setError(error)
    );
  }
}
