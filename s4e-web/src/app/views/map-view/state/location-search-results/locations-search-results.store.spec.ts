import {LocationSearchResultsStore} from './locations-search-results.store';

describe('LocationSearchResultsStore', () => {
  let store: LocationSearchResultsStore;

  beforeEach(() => {
    store = new LocationSearchResultsStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
