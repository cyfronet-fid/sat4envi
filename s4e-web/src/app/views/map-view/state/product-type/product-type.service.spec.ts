import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ProductTypeService} from './product-type.service';
import {ProductTypeStore} from './product-type.store';
import {ProductTypeQuery} from './product-type.query';
import {ProductService} from '../product/product.service';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {ProductQuery} from '../product/product.query';
import {LegendQuery} from '../legend/legend.query';
import {ProductStore} from '../product/product.store';
import {LegendService} from '../legend/legend.service';

describe('ProductTypeService', () => {
  let productTypeService: ProductTypeService;
  let productTypeStore: ProductTypeStore;
  let productTypeQuery: ProductTypeQuery;
  let productQuery: ProductQuery;
  let productStore: ProductStore;
  let legendQuery: LegendQuery;
  let legendService: LegendService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ProductTypeService,
        ProductTypeStore,
        ProductQuery,
        ProductStore,
        LegendQuery,
        LegendService,
        TestingConfigProvider,
        HttpClientTestingModule,
        ProductTypeQuery,
        ProductService
      ],
      imports: [HttpClientTestingModule]
    });

    productTypeQuery = TestBed.get(ProductTypeQuery);
    productTypeService = TestBed.get(ProductTypeService);
    productTypeStore = TestBed.get(ProductTypeStore);
    productQuery = TestBed.get(ProductQuery);
    legendQuery = TestBed.get(LegendQuery);
    productStore = TestBed.get(ProductStore);
    legendService = TestBed.get(LegendService);
  });

  it('should be created', () => {
    expect(productTypeService).toBeDefined();
  });

  describe('setActive', () => {
    it('with null should clear stores', () => {
      const spy1 = spyOn(productStore, 'setActive').and.stub();
      const spy2 = spyOn(legendService, 'set').and.stub();
      const spy3 = spyOn(productTypeStore, 'setActive').and.stub();

      productTypeService.setActive(null);

      expect(spy1).toHaveBeenCalledWith(null);
      expect(spy2).toHaveBeenCalledWith(null);
      expect(spy3).toHaveBeenCalledWith(null);
    });
  });
});
