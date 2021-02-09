/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Store, EntityStore, EntityState} from '@datorama/akita';
import {catchError, shareReplay, tap, finalize} from 'rxjs/operators';
import {throwError, Observable, of, isObservable} from 'rxjs';

export function handleHttpRequest$<T, S>(
  store: EntityStore<EntityState<S>, S> | Store<S>
): (source: Observable<T>) => Observable<T | any | null> {
  return function (source: Observable<T>) {
    store.setLoading(true);
    store.setError(null);

    return source.pipe(
      catchError(error => {
        store.setError(error);
        return throwError(error);
      }),
      finalize(() => store.setLoading(false)),
      shareReplay(1)
    );
  };
}
