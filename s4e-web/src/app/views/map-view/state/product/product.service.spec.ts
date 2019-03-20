import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {ProductStore} from './product.store';
import {TestingConstantsProvider} from '../../../../app.constants.spec';
import {ProductQuery} from './product.query';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {RecentViewStore} from '../recent-view/recent-view.store';
import {GranuleService} from '../granule/granule.service';

describe('ProductService', () => {
  let productService: ProductService;
  let productStore: ProductStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        ProductStore,
        TestingConstantsProvider,
        HttpClientTestingModule,
        ProductQuery,
        RecentViewQuery,
        RecentViewStore,
        GranuleService
      ],
      imports: [HttpClientTestingModule]
    });

    productService = TestBed.get(ProductService);
    productStore = TestBed.get(ProductStore);
  });

  it('should be created', () => {
    expect(productService).toBeDefined();
  });

});
