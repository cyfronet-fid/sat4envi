import {MapStore} from './map.store';

describe('MapStore', () => {
  let store: MapStore;

  beforeEach(() => {
    store = new MapStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
