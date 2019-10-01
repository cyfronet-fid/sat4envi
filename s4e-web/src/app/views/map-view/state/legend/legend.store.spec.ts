import { LegendStore } from './legend.store';

describe('LegendStore', () => {
  let store: LegendStore;

  beforeEach(() => {
    store = new LegendStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
