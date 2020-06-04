import { InstitutionQuery } from './institution.query';
import { InstitutionStore } from './institution.store';

describe('InstitutionQuery', () => {
  let query: InstitutionQuery;

  beforeEach(() => {
    query = new InstitutionQuery(new InstitutionStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
