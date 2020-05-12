import { Store } from '@datorama/akita';
import { catchError } from 'rxjs/operators';
import { throwError, OperatorFunction } from 'rxjs';

export function catchErrorAndHandleStore<T>(store: Store<any>): OperatorFunction<T, T> {
  return catchError(error => {
      store.setError(error);
      return throwError(error);
    });
}
