import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {ProductStore} from './product.store';
import {ProductTypeQuery} from '../product-type/product-type.query';
import {ProductTypeStore} from '../product-type/product-type.store';
import {ProductQuery} from './product.query';
import {take, toArray} from 'rxjs/operators';
import {ProductFactory} from './product.factory.spec';
import {InjectorModule} from '../../../../common/injector.module';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {LegendFactory} from '../legend/legend.factory.spec';
import {LegendQuery} from '../legend/legend.query';
import {LegendStore} from '../legend/legend.store';
import {ProductTypeFactory} from '../product-type/product-type.factory.spec';

describe('ProductService', () => {
  let productService: ProductService;
  let productStore: ProductStore;
  let productQuery: ProductQuery;
  let legendQuery: LegendQuery;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        ProductStore,
        LegendStore,
        LegendQuery,
        ProductTypeQuery,
        ProductTypeStore,
        TestingConfigProvider],
      imports: [HttpClientTestingModule, InjectorModule]
    });

    http = TestBed.get(HttpTestingController);
    productService = TestBed.get(ProductService);
    productStore = TestBed.get(ProductStore);
    productQuery = TestBed.get(ProductQuery);
    legendQuery = TestBed.get(LegendQuery);
  });

  it('should create', () => {
    expect(productService).toBeTruthy();
  });

  describe('setActive', () => {
    it('should set legend to Product\'s', () => {
      const legend = LegendFactory.build();
      const product = ProductFactory.build({legend});
      productStore.set([product]);
      productService.setActive(product.id);

      expect(legendQuery.getValue().legend).toEqual(legend);
    });

    it('should set legend to ProductType\'s if Product does not have one', () => {
      const legend = LegendFactory.build();
      const product = ProductFactory.build();
      const productType = ProductTypeFactory.build({legend, productIds: [product.id]});
      const productTypeStore: ProductTypeStore = TestBed.get(ProductTypeStore);
      productStore.set([product]);
      productTypeStore.set([productType]);
      productTypeStore.setActive(productType.id);

      productService.setActive(product.id);
      expect(legendQuery.getValue().legend).toEqual(legend);
    });
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
      const productType = ProductTypeFactory.build();
      const productTypeId = productType.id;
      const productTypeStore: ProductTypeStore = TestBed.get(ProductTypeStore);
      productTypeStore.add(productType);

      productQuery.selectAll().pipe(take(3), toArray()).subscribe(data => {
        expect(data).toEqual([[], [], [productR]]);
        done();
      });

      const productR = ProductFactory.build();

      productService.get(productTypeId);

      const r = http.expectOne(`api/v1/products/productTypeId/${productTypeId}`);
      r.flush([productR]);
    });
  });
});
