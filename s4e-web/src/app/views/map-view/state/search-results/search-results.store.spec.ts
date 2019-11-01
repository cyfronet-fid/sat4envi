import {SearchResultsStore} from './search-results.store';

describe('ProductTypeStore', () => {
  let store: SearchResultsStore;

  beforeEach(() => {
    store = new SearchResultsStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
