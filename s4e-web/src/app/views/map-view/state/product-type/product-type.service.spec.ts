import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ProductTypeService} from './product-type.service';
import {ProductTypeStore} from './product-type.store';
import {ProductTypeQuery} from './product-type.query';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {RecentViewStore} from '../recent-view/recent-view.store';
import {ProductService} from '../product/product.service';
import {TestingConfigProvider} from '../../../../app.configuration.spec';

describe('ProductTypeService', () => {
  let productService: ProductTypeService;
  let productStore: ProductTypeStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductTypeService,
        ProductTypeStore,
        TestingConfigProvider,
        HttpClientTestingModule,
        ProductTypeQuery,
        RecentViewQuery,
        RecentViewStore,
        ProductService
      ],
      imports: [HttpClientTestingModule]
    });

    productService = TestBed.get(ProductTypeService);
    productStore = TestBed.get(ProductTypeStore);
  });

  it('should be created', () => {
    expect(productService).toBeDefined();
  });

});
