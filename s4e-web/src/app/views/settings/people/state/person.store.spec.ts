import { PersonStore } from './person.store';

describe('PersonStore', () => {
  let store: PersonStore;

  beforeEach(() => {
    store = new PersonStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
