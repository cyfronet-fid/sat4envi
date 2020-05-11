import {Injectable} from '@angular/core';
import {QueryEntity, EntityStore} from '@datorama/akita';
import {SearchResultsState} from './search-result.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SearchResultsQuery<T> extends QueryEntity<SearchResultsState<T>, T> {

  constructor(protected store: EntityStore<SearchResultsState<T>, T>) {
    super(store);
  }

  selectIsOpen(): Observable<boolean> {
    return this.select(state  => state.isOpen);
  }

  selectLocation(): Observable<T | null> {
    return this.select(state  => state.searchResult);
  }

}
