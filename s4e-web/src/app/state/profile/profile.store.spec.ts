import { ProfileStore } from './profile.store';

describe('ProfileStore', () => {
  let store: ProfileStore;

  beforeEach(() => {
    store = new ProfileStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
