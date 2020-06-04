import { InstitutionsSearchResultsQuery } from './institutions-search-results.query';
import { InstitutionsSearchResultsStore } from './institutions-search-results.store';


describe('LocationSearchResultsQuery', () => {
  let query: InstitutionsSearchResultsQuery;

  beforeEach(() => {
    query = new InstitutionsSearchResultsQuery(new InstitutionsSearchResultsStore());
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });
});
