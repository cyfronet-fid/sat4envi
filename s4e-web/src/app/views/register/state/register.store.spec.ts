import { RegisterStore } from './register.store';

describe('RegisterStore', () => {
  let store: RegisterStore;

  beforeEach(() => {
    store = new RegisterStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
