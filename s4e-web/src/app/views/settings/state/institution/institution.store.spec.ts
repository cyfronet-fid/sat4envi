import { InstitutionStore } from './institution.store';

describe('InstitutionStore', () => {
  let store: InstitutionStore;

  beforeEach(() => {
    store = new InstitutionStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
