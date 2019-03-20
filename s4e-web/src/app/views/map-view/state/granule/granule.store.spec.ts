import { GranuleStore } from './granule.store';

describe('GranuleStore', () => {
  let store: GranuleStore;

  beforeEach(() => {
    store = new GranuleStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
