import { ProductQuery } from './product.query';
import { ProductStore } from './product.store';

describe('ProductQuery', () => {
  let query: ProductQuery;

  beforeEach(() => {
    query = new ProductQuery(new ProductStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
