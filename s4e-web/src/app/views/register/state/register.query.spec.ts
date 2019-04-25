import { RegisterQuery } from './register.query';
import { RegisterStore } from './register.store';

describe('RegisterQuery', () => {
  let query: RegisterQuery;

  beforeEach(() => {
    query = new RegisterQuery(new RegisterStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
