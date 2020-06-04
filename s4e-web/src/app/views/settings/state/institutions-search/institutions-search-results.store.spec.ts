import {InstitutionsSearchResultsStore} from './institutions-search-results.store';

describe('InstitutionsSearchResultsStore', () => {
  let store: InstitutionsSearchResultsStore;

  beforeEach(() => {
    store = new InstitutionsSearchResultsStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
