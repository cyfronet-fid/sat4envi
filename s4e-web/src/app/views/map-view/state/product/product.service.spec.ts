import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {ProductStore} from './product.store';
import {ProductQuery} from './product.query';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {LegendQuery} from '../legend/legend.query';
import {LegendService} from '../legend/legend.service';
import {SceneQuery} from '../scene/scene.query.service';
import {SceneStore} from '../scene/scene.store.service';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {Router} from '@angular/router';
import {PRODUCT_MODE_FAVOURITE, PRODUCT_MODE_QUERY_KEY} from './product.model';

describe('ProductService', () => {
  let productService: ProductService;
  let productStore: ProductStore;
  let productQuery: ProductQuery;
  let sceneQuery: SceneQuery;
  let sceneStore: SceneStore;
  let legendQuery: LegendQuery;
  let legendService: LegendService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TestingConfigProvider],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    productQuery = TestBed.get(ProductQuery);
    productService = TestBed.get(ProductService);
    productStore = TestBed.get(ProductStore);
    sceneQuery = TestBed.get(SceneQuery);
    legendQuery = TestBed.get(LegendQuery);
    sceneStore = TestBed.get(SceneStore);
    legendService = TestBed.get(LegendService);
  });

  it('should be created', () => {
    expect(productService).toBeDefined();
  });

  describe('setActive', () => {
    it('with null should clear stores', () => {
      const spy1 = spyOn(sceneStore, 'setActive').and.stub();
      const spy2 = spyOn(legendService, 'set').and.stub();
      const spy3 = spyOn(productStore, 'setActive').and.stub();

      productService.setActive(null);

      expect(spy1).toHaveBeenCalledWith(null);
      expect(spy2).toHaveBeenCalledWith(null);
      expect(spy3).toHaveBeenCalledWith(null);
    });
  });

  it('setFavouriteMode', () => {
    const router: Router = TestBed.get(Router);
    const spy = spyOn(router, 'navigate');
    productService.setFavouriteMode(true);
    expect(spy).toHaveBeenCalledWith([], {
      queryParamsHandling: 'merge',
      queryParams: {[PRODUCT_MODE_QUERY_KEY]: PRODUCT_MODE_FAVOURITE}
    });

    productService.setFavouriteMode(false);

    expect(spy).toHaveBeenCalledWith([], {
      queryParamsHandling: 'merge',
      queryParams: {[PRODUCT_MODE_QUERY_KEY]: ''}
    });
  });
});
