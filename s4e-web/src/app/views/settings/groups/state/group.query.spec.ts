import { GroupQuery } from './group.query';
import { GroupStore } from './group.store';

describe('GroupQuery', () => {
  let query: GroupQuery;

  beforeEach(() => {
    query = new GroupQuery(new GroupStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
