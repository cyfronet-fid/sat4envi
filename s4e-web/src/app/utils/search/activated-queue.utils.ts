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
