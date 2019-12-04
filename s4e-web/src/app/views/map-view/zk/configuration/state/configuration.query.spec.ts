import { ConfigurationQuery } from './configuration.query';
import { ConfigurationStore } from './configuration.store';

describe('ConfigurationQuery', () => {
  let query: ConfigurationQuery;

  beforeEach(() => {
    query = new ConfigurationQuery(new ConfigurationStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
