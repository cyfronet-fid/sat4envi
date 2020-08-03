import { Store } from '@datorama/akita';
import { catchError, finalize, shareReplay, map } from 'rxjs/operators';
import { throwError, OperatorFunction, Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export function httpGetRequest$<T>(http: HttpClient, url: string, store: Store<any>): Observable<T | null> {
  store.setLoading(true);
  store.setError(null);
  return (!!url && http.get<T>(url) || of(null))
    .pipe(handleHttpGetRequest(store));
}

export function httpPutRequest$<T>(http: HttpClient, url: string, object: T, store: Store<any>) {
  store.setLoading(true);
  store.setError(null);
  return http.put<T>(url, object)
    .pipe(handleHttpGetRequest(store));
}

export function httpDeleteRequest$(http: HttpClient, url: string, store: Store<any>) {
  store.setLoading(true);
  store.setError(null);
  return http.delete(url)
    .pipe(handleHttpGetRequest(store));
}

export function httpPostRequest$<T>(http: HttpClient, url: string, object: T, store: Store<any>) {
  store.setLoading(true);
  store.setError(null);
  return http.post<T>(url, object)
    .pipe(handleHttpGetRequest(store));
}

export function handleHttpGetRequest(store: Store<any>) {
  return function<T>(source: Observable<T>) {
    return source
      .pipe(
        catchErrorAndHandleStore(store),
        finalize(() => store.setLoading(false)),
        shareReplay(1)
      );
  };
}

export function catchErrorAndHandleStore<T>(store: Store<any>): OperatorFunction<T, T> {
  return catchError(error => {
      store.setError(error);
      return throwError(error);
    });
}
