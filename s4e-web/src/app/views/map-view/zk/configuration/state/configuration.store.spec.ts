import { ConfigurationStore } from './configuration.store';

describe('ConfigurationStore', () => {
  let store: ConfigurationStore;

  beforeEach(() => {
    store = new ConfigurationStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
