import { MapQuery } from './map.query';
import { MapStore } from './map.store';

describe('MapQuery', () => {
  let query: MapQuery;

  beforeEach(() => {
    query = new MapQuery(new MapStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
