import { ProductTypeQuery } from './product-type.query';
import { ProductTypeStore } from './product-type.store';
import {ProductQuery} from '../product/product-query.service';
import {ProductStore} from '../product/product-store.service';

describe('ProductTypeQuery', () => {
  let query: ProductTypeQuery;

  beforeEach(() => {
    query = new ProductTypeQuery(new ProductTypeStore, new ProductQuery(new ProductStore()));
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
