/*
 * Copyright 2020 ACC Cyfronet AGH
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

import { QueryEntity, EntityStore } from '@datorama/akita';

export class ActivatedQueue {
  protected _query: QueryEntity<any, any>;
  protected _store: EntityStore<any, any>;

  constructor(query: QueryEntity<any, any>, store: EntityStore<any, any>, protected _loop: boolean = true) {
    this._query = query;
    this._store = store;
  }

  next(): boolean {
    return this._moveBy(1);
  }

  previous(): boolean {
    return this._moveBy(-1);
  }

  protected _moveBy(direction: -1 | 1): boolean {
    const results = this._query.getAll();
    const active = this._query.getActive();

    if (!active) {
      const firstResultId = !!results && results.length > 0
        && !!results[0].id && results[0].id;
      this._store.setActive(firstResultId || null);
      return results.length > 0;
    }

    let nextIndex = results.indexOf(active) + direction;

    if ((nextIndex === -1 && !this._loop) || (nextIndex === results.length && !this._loop)) {
      return false;
    }

    nextIndex = nextIndex === -1 ? results.length - 1 : nextIndex;
    nextIndex = nextIndex === results.length ? 0 : nextIndex;

    this._store.setActive(results[nextIndex].id);
    return true;
  }
}
