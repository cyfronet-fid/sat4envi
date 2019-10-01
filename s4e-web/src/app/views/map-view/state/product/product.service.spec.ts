import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {ProductStore} from './product.store';
import {ProductTypeQuery} from '../product-type/product-type.query';
import {ProductTypeStore} from '../product-type/product-type.store';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {RecentViewStore} from '../recent-view/recent-view.store';
import {ProductQuery} from './product.query';
import {take, toArray} from 'rxjs/operators';
import {ProductResponseFactory} from './product.factory.spec';
import {deserializeJsonResponse} from '../../../../utils/miscellaneous/miscellaneous';
import {ProductResponse} from './product.model';
import {InjectorModule} from '../../../../common/injector.module';
import {TestingConfigProvider} from '../../../../app.configuration.spec';

describe('ProductService', () => {
  let productService: ProductService;
  let productStore: ProductStore;
  let productQuery: ProductQuery;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        ProductStore,
        ProductTypeQuery,
        ProductTypeStore,
        RecentViewQuery,
        RecentViewStore,
        TestingConfigProvider],
      imports: [HttpClientTestingModule, InjectorModule]
    });

    http = TestBed.get(HttpTestingController);
    productService = TestBed.get(ProductService);
    productStore = TestBed.get(ProductStore);
    productQuery = TestBed.get(ProductQuery);
  });

  it('should create', () => {
    expect(productService).toBeTruthy();
  });

  describe('get', () => {
    it('loading should be set', (done) => {
      const productId = 1;
      const stream = productQuery.selectLoading();

      stream.pipe(take(2), toArray()).subscribe(data => {
        expect(data).toEqual([true, false]);
        done();
      });

      productService.get(productId);

      const r = http.expectOne(`api/v1/products/productTypeId/${productId}`);
      r.flush(null);
    });

    it('should call http endpoint', () => {
      const productId = 1;
      productService.get(productId);
      http.expectOne(`api/v1/products/productTypeId/${productId}`);
    });

    it('should set state in store', (done) => {
      const productId = 1;

      productQuery.selectAll().pipe(take(2), toArray()).subscribe(data => {
        expect(data).toEqual([[], [deserializeJsonResponse(productR, ProductResponse)]]);
        done();
      });

      const productR = ProductResponseFactory.build();

      productService.get(productId);

      const r = http.expectOne(`api/v1/products/productTypeId/${productId}`);
      r.flush(productR);
    });
  });
});
