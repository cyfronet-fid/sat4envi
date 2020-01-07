import { SentinelSearchStore } from './sentinel-search.store';

describe('SentinelSearchResultStore', () => {
  let store: SentinelSearchStore;

  beforeEach(() => {
    store = new SentinelSearchStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
