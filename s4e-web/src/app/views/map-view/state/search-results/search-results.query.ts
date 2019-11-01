import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {SearchResultsState, SearchResultsStore} from './search-results.store';
import {SearchResult} from './search-result.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SearchResultsQuery extends QueryEntity<SearchResultsState, SearchResult> {

  constructor(protected store: SearchResultsStore) {
    super(store);
  }

  selectIsOpen(): Observable<boolean> {
    return this.select(state  => state.isOpen);
  }
}
