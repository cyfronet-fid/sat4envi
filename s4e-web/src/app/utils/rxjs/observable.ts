import {Observable} from 'rxjs';
import {shareReplay} from 'rxjs/operators';

export function pubNoSub$<T = any>(obs: Observable<T>): Observable<T> {
  const r = obs.pipe(shareReplay(1));
  r.subscribe();
  return r;
}
