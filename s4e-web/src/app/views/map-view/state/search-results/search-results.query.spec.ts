import {SearchResultsQuery} from './search-results.query';
import { EntityStore } from '@datorama/akita';
import { SearchResultsState } from './search-result.model';

describe('SearchResultsQuery', () => {
  let query: SearchResultsQuery<any>;

  beforeEach(() => {
    query = new SearchResultsQuery<any>(new EntityStore<SearchResultsState<any>, any>());
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
