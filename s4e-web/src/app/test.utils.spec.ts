import {Observable} from 'rxjs';
import {take, toArray} from 'rxjs/operators';

export function toTestPromise<T>(observable: Observable<T>, takeLast: number = 1): Promise<T|T[]> {
  observable = observable.pipe(take(takeLast))

  if (takeLast === 1) {
    return observable.toPromise();
  }
  return observable.pipe(toArray()).toPromise();
}
