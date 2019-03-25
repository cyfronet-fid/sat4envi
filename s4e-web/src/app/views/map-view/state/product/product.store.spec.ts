import { ProductStore } from './product-store.service';

describe('ProductStore', () => {
  let store: ProductStore;

  beforeEach(() => {
    store = new ProductStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
