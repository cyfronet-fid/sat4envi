import { ProductQuery } from './product-query.service';
import { ProductStore } from './product-store.service';

describe('ProductQuery', () => {
  let query: ProductQuery;

  beforeEach(() => {
    query = new ProductQuery(new ProductStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
