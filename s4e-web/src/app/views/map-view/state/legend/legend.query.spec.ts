import { LegendQuery } from './legend.query';
import { LegendStore } from './legend.store';

describe('LegendQuery', () => {
  let query: LegendQuery;

  beforeEach(() => {
    query = new LegendQuery(new LegendStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
