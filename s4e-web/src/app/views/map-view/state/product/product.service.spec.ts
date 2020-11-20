import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ProductService} from './product.service';
import {ProductStore} from './product.store';
import {ProductQuery} from './product.query';
import {LegendQuery} from '../legend/legend.query';
import {LegendService} from '../legend/legend.service';
import {SceneQuery} from '../scene/scene.query';
import {SceneStore} from '../scene/scene.store.service';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {Router} from '@angular/router';
import {PRODUCT_MODE_FAVOURITE, PRODUCT_MODE_QUERY_KEY} from './product.model';
import {ProductFactory} from './product.factory.spec';
import {SceneService} from '../scene/scene.service';
import * as dateUtils from '../../../../utils/miscellaneous/date-utils'
import environment from '../../../../../environments/environment';
import {SceneFactory} from '../scene/scene.factory.spec';
import {distinctUntilChanged, map, take, toArray} from 'rxjs/operators';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {of} from 'rxjs';

describe('ProductService', () => {
  let productService: ProductService;
  let productStore: ProductStore;
  let productQuery: ProductQuery;
  let sceneService: SceneService;
  let sceneQuery: SceneQuery;
  let sceneStore: SceneStore;
  let legendQuery: LegendQuery;
  let legendService: LegendService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    productQuery = TestBed.get(ProductQuery);
    productService = TestBed.get(ProductService);
    productStore = TestBed.get(ProductStore);
    sceneQuery = TestBed.get(SceneQuery);
    sceneService = TestBed.get(SceneService);
    legendQuery = TestBed.get(LegendQuery);
    sceneStore = TestBed.get(SceneStore);
    legendService = TestBed.get(LegendService);
    http = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(productService).toBeDefined();
  });

  describe('setActive', () => {
    it('with null should clear stores', () => {
      const spy1 = spyOn(sceneStore, 'setActive').and.stub();
      const spy2 = spyOn(legendService, 'set').and.stub();
      const spy3 = spyOn(productStore, 'setActive').and.stub();

      productService.setActive$(null);

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

  describe('getLastAvailableScene', () => {
    it('should handle active product == null', () => {
      productStore.set([ProductFactory.build()])
      productStore.setActive(null);
      expect(() => productService.getLastAvailableScene$()).not.toThrow();
    });

    it('should get last available', () => {
      spyOn(dateUtils, 'timezone').and.returnValue('Europe/Warsaw')
      const product = ProductFactory.build();
      const scene = SceneFactory.build()
      productStore.set([product])
      productStore.setActive(product.id)

      const promise = productQuery.ui.selectEntity(product.id).pipe(
        map(productUi => productUi.isLoading),
        distinctUntilChanged(),
        take(3),
        toArray()
      ).toPromise();

      spyOn(productService, 'setSelectedDate')
      spyOn(sceneService, 'get').and.returnValue(of(true))
      spyOn(sceneService, 'setActive').and.stub()

      productService.getLastAvailableScene$()
        .subscribe(async () => {
          const req = http.expectOne(`${environment.apiPrefixV1}/products/${product.id}/scenes/most-recent?timeZone=${dateUtils.timezone()}`)
          expect(req.request.method).toBe('GET');
          req.flush({sceneId: scene.id, timestamp: scene.timestamp});

          expect(productService.setSelectedDate).toHaveBeenCalledWith(scene.timestamp);
          expect(sceneService.get).toHaveBeenCalledWith(product, scene.timestamp.substr(0, 10));
          expect(sceneService.setActive).toHaveBeenCalledWith(scene.id);

          expect(await promise).toEqual([false, true, false]);
          http.verify();
        });
    });
  });
});
