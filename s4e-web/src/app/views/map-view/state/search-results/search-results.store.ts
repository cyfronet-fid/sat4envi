import {Injectable} from '@angular/core';
import {EntityStore, StoreConfig} from '@datorama/akita';
import {createInitialState, SearchResult, SearchResultsState} from './search-result.model';


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'SearchResults'})
export class SearchResultsStore extends EntityStore<SearchResultsState, SearchResult> {
  constructor() {
    super(createInitialState());
  }
}

