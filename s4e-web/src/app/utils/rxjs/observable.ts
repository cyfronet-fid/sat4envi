import {Observable} from 'rxjs';
import {distinctUntilChanged, filter, map, shareReplay, tap} from 'rxjs/operators';
import equal from 'fast-deep-equal';

export function pubNoSub$<T = any>(obs: Observable<T>): Observable<T> {
  const r = obs.pipe(shareReplay(1));
  r.subscribe();
  return r;
}

export function mapAnyTrue<T = any>() {
  return (input$: Observable<T | T[]>) => input$.pipe(
    map(obj => Array.isArray(obj) ? obj : [obj]),
    map(arr => arr.filter(element => element).length > 0),
  );
}

export function mapAllTrue() {
  return (input$: Observable<boolean[]>) => input$.pipe(
    map(arr => arr.findIndex(el => el != true) === -1)
  );
}

export function filterTrue<T>() {
  return (input$: Observable<T>) => input$.pipe(filter(obj => !!obj));
}

export function filterFalse<T>() {
  return (input$: Observable<T>) => input$.pipe(filter(obj => !obj));
}

export function filterNull<T>() {
  return (input$: Observable<T>) => input$.pipe(filter(obj => obj == null));
}

export function filterNotNull<T>() {
  return (input$: Observable<T>) => input$.pipe(filter(obj => obj != null));
}

export function logIt<T>(identifier?: string): (source: Observable<T>) => Observable<T> {
  return function (source: Observable<T>) {
    return source.pipe(
      tap(val => identifier ? console.log(identifier, val) : console.log(val))
    )
  }
}

export function distinctUntilChangedDE<T>() {
  return (source: Observable<T>) => {
    return source.pipe(distinctUntilChanged((a, b) => equal(a, b)))
  }
}
