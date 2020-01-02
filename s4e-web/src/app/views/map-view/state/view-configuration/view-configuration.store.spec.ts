import { ViewConfigurationStore } from './view-configuration.store';

describe('ViewConfigurationStore', () => {
  let store: ViewConfigurationStore;

  beforeEach(() => {
    store = new ViewConfigurationStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
