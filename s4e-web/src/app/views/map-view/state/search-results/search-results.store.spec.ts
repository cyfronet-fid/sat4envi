import {SearchResultsStore} from './search-results.store';

describe('SearchResultsStore', () => {
  let store: SearchResultsStore;

  beforeEach(() => {
    store = new SearchResultsStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
