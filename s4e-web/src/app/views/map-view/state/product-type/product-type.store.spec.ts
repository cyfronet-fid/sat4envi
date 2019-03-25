import { ProductTypeStore } from './product-type-store.service';

describe('ProductTypeStore', () => {
  let store: ProductTypeStore;

  beforeEach(() => {
    store = new ProductTypeStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
