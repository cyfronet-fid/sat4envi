import { SentinelSearchQuery } from './sentinel-search.query';
import { SentinelSearchStore } from './sentinel-search.store';

describe('SentinelSearchResultQuery', () => {
  let query: SentinelSearchQuery;

  beforeEach(() => {
    query = new SentinelSearchQuery(new SentinelSearchStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
