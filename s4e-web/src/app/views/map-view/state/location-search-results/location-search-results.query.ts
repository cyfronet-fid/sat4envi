import {Injectable} from '@angular/core';
import {LocationSearchResultsStore} from './locations-search-results.store';
import {LocationSearchResult} from './location-search-result.model';
import { SearchResultsQuery } from '../search-results/search-results.query';

@Injectable({
  providedIn: 'root'
})
export class LocationSearchResultsQuery extends SearchResultsQuery<LocationSearchResult> {
  constructor(protected store: LocationSearchResultsStore) {
    super(store);
  }
}
