import {Injectable} from '@angular/core';
import {EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {SearchResult} from './search-result.model';


export interface SearchResultsState extends EntityState<SearchResult> {
  isOpen: boolean;
  queryString: string;
}

export function createInitialState(): SearchResultsState {
  return {
    isOpen: false,
    queryString: ''
  };
}

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'SearchResults'})
export class SearchResultsStore extends EntityStore<SearchResultsState, SearchResult> {

  constructor() {
    super(createInitialState());
  }

}

