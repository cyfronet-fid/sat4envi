import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ViewConfigurationStore} from './view-configuration.store';
import {ViewConfiguration} from './view-configuration.model';
import {finalize, map, shareReplay} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {IPageableResponse} from '../../../../state/pagable.model';
import {ViewConfigurationQuery} from './view-configuration.query';
import {catchErrorAndHandleStore} from '../../../../common/store.util';
import environment from 'src/environments/environment';

@Injectable({providedIn: 'root'})
export class ViewConfigurationService {

  constructor(private store: ViewConfigurationStore,
              private query: ViewConfigurationQuery,
              private http: HttpClient) {
  }

  add$(viewConfiguration: ViewConfiguration): Observable<boolean> {
    this.store.setError(null);
    this.store.setLoading(true);

    const r = this.http.post<ViewConfiguration>(`${environment.apiPrefixV1}/saved-views`, viewConfiguration)
      .pipe(
        map(r => true),
        catchErrorAndHandleStore(this.store),
        finalize(() => this.store.setLoading(false)),
        shareReplay(1)
      );
    r.subscribe();
    return r;
  }

  delete(uuid: string) {
    this.store.setError(null);
    this.store.setLoading(true);
    this.http.delete(`${environment.apiPrefixV1}/saved-views/${uuid}`, {
      headers: {'Content-Type': 'application/json'}
    }).pipe(
      catchErrorAndHandleStore(this.store),
      finalize(() => this.store.setLoading(false))
    )
      .subscribe(() => this.store.remove(uuid));
  }

  get() {
    this.store.setError(null);
    this.store.setLoading(true);
    this.http.get<IPageableResponse<ViewConfiguration>>(`${environment.apiPrefixV1}/saved-views`, {
      headers: {'Content-Type': 'application/json'}
    }).pipe(
      finalize(() => this.store.setLoading(false)),
      catchErrorAndHandleStore(this.store),
    ).subscribe(data => this.store.set(data.content));
  }
}
