import { GroupStore } from './group.store';

describe('GroupStore', () => {
  let store: GroupStore;

  beforeEach(() => {
    store = new GroupStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
