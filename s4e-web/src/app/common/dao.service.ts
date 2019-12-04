import {EntityState, EntityStore, getIDType, ID} from '@datorama/akita';
import {Observable, throwError} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {catchError, finalize, publish, shareReplay, tap} from 'rxjs/operators';

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
        catchError(error => {
          this.store.setError(error);
          return throwError(error)
        }),
        shareReplay(1)
      );

    r.subscribe();
    return r
  }

  fetchAll$(url?: string): Observable<T[]> {
    this.store.setLoading(true);
    this.store.setError(null);
    const r = this.http.get<T[]>(url || this.url)
      .pipe(
        finalize(() => this.store.setLoading(false)),
        tap(data => this.store.set(data)),
        catchError(error => {
          this.store.setError(error);
          return throwError(error)
        }),
        shareReplay(1)
      );

    r.subscribe();
    return r
  }
}
