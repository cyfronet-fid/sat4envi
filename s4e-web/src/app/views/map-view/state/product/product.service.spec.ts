import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { ProductStore } from './product-store.service';
import {TestingConstantsProvider} from '../../../../app.constants.spec';
import {ProductTypeQuery} from '../product-type/product-type-query.service';
import {ProductTypeStore} from '../product-type/product-type-store.service';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {RecentViewStore} from '../recent-view/recent-view.store';

describe('ProductService', () => {
  let productService: ProductService;
  let productStore: ProductStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        ProductStore,
        ProductTypeQuery,
        ProductTypeStore,
        RecentViewQuery,
        RecentViewStore,
        TestingConstantsProvider],
      imports: [ HttpClientTestingModule ]
    });

    productService = TestBed.get(ProductService);
    productStore = TestBed.get(ProductStore);
  });

  it('get should work', () => {
    // :TODO add test
  });

  it('setActive should work', () => {
    // :TODO add test
  });

});
