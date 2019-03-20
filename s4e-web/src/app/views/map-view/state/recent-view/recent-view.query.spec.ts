import { RecentViewQuery } from './recent-view.query';
import { RecentViewStore } from './recent-view.store';

describe('RecentViewQuery', () => {
  let query: RecentViewQuery;

  beforeEach(() => {
    query = new RecentViewQuery(new RecentViewStore, null, null);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
