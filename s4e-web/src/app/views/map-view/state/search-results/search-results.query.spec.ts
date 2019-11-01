import {SearchResultsQuery} from './search-results.query';
import {SearchResultsStore} from './search-results.store';

describe('SearchResultsQuery', () => {
  let query: SearchResultsQuery;

  beforeEach(() => {
    query = new SearchResultsQuery(new SearchResultsStore());
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
