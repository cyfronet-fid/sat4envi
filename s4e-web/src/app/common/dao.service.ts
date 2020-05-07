import {EntityState, EntityStore, getIDType} from '@datorama/akita';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import { finalize, shareReplay, tap} from 'rxjs/operators';
import { catchErrorAndHandleStore } from './store.util';

export abstract class Dao<T, S extends EntityState<T>, Store extends EntityStore<S, T>> {
  protected constructor(protected http: HttpClient,
                        protected url: string,
                        protected store: Store) {
  }

  fetch$(id: getIDType<S>, url?: string): Observable<T> {
    this.store.setLoading(true);
    this.store.setError(null);
    const r =this.http.get<T>(`${url || this.url}/${id}`)
      .pipe(
        finalize(() => this.store.setLoading(false)),
        tap(data => this.store.upsert(id, data)),
        catchErrorAndHandleStore(this.store),
        shareReplay(1)
      );

    r.subscribe();
    return r;
  }

  fetchAll$(url?: string): Observable<T[]> {
    this.store.setLoading(true);
    this.store.setError(null);
    const r = this.http.get<T[]>(url || this.url)
      .pipe(
        finalize(() => this.store.setLoading(false)),
        tap(data => this.store.set(data)),
        catchErrorAndHandleStore(this.store),
        shareReplay(1)
      );

    r.subscribe();
    return r;
  }
}
