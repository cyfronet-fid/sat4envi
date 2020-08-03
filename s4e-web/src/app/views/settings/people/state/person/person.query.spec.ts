import { PersonQuery } from './person.query';
import { PersonStore } from './person.store';

describe('PersonQuery', () => {
  let query: PersonQuery;

  beforeEach(() => {
    query = new PersonQuery(new PersonStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
