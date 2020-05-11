import {Injectable} from '@angular/core';
import {StoreConfig, EntityStore} from '@datorama/akita';
import {LocationSearchResult} from './location-search-result.model';
import { createInitialState, SearchResultsState } from '../search-results/search-result.model';


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'LocationSearchResults'})
export class LocationSearchResultsStore extends EntityStore<SearchResultsState<LocationSearchResult>, LocationSearchResult> {
  constructor() {
    super(createInitialState<LocationSearchResult>());
  }
}

