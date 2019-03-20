import { OverlayStore } from './overlay.store';

describe('OverlayStore', () => {
  let store: OverlayStore;

  beforeEach(() => {
    store = new OverlayStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
