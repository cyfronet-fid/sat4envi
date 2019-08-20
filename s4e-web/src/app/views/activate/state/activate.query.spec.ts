import {ActivateQuery} from './activate.query';
import {ActivateStore} from './activate.store';

describe('ActivateQuery', () => {
  let query: ActivateQuery;

  beforeEach(() => {
    query = new ActivateQuery(new ActivateStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
