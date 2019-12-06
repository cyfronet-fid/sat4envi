import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {SearchResultsStore} from './search-results.store';
import {SearchResult, SearchResultsState} from './search-result.model';
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

  selectLocation(): Observable<SearchResult|null> {
    return this.select(state  => state.selectedLocation);
  }

}
