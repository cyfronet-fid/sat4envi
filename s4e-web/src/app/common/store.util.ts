import { Store, EntityStore, EntityState } from '@datorama/akita';
import { catchError, shareReplay, tap, finalize } from 'rxjs/operators';
import { throwError, Observable, of, isObservable } from 'rxjs';

export function handleHttpRequest$<T, S>(
  store: EntityStore<EntityState<S>, S> | Store<S>
): (source: Observable<T>) => Observable<T|any|null> {
  return function(source: Observable<T>) {
    store.setLoading(true);
    store.setError(null);

    return source
      .pipe(
        catchError(error => {
          store.setError(error);
          return throwError(error);
        }),
        finalize(() => store.setLoading(false)),
        shareReplay(1)
      );
  };
}
