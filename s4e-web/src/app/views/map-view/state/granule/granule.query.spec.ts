import { GranuleQuery } from './granule.query';
import { GranuleStore } from './granule.store';

describe('GranuleQuery', () => {
  let query: GranuleQuery;

  beforeEach(() => {
    query = new GranuleQuery(new GranuleStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
