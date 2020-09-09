import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {SceneService} from './scene.service';
import {SceneStore} from './scene.store.service';
import {SceneQuery} from './scene.query';
import {take, toArray} from 'rxjs/operators';
import {SceneFactory} from './scene.factory.spec';
import {LegendFactory} from '../legend/legend.factory.spec';
import {LegendQuery} from '../legend/legend.query';
import {LegendStore} from '../legend/legend.store';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {ProductFactory} from '../product/product.factory.spec';
import environment from 'src/environments/environment';
import {timezone} from '../../../../utils/miscellaneous/date-utils';

describe('SceneService', () => {
  let sceneService: SceneService;
  let sceneStore: SceneStore;
  let sceneQuery: SceneQuery;
  let legendQuery: LegendQuery;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SceneService,
        SceneStore,
        LegendStore,
        LegendQuery,
        ProductQuery,
        ProductStore],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    sceneService = TestBed.get(SceneService);
    sceneStore = TestBed.get(SceneStore);
    sceneQuery = TestBed.get(SceneQuery);
    legendQuery = TestBed.get(LegendQuery);
  });

  it('should create', () => {
    expect(sceneService).toBeTruthy();
  });

  describe('setActive', () => {
    it('should set legend to Scene\'s', () => {
      const legend = LegendFactory.build();
      const product = SceneFactory.build({legend});
      sceneStore.set([product]);
      sceneService.setActive(product.id);

      expect(legendQuery.getValue().legend).toEqual(legend);
    });

    it('should set legend to Product\'s if Scene does not have one', () => {
      const legend = LegendFactory.build();
      const scene = SceneFactory.build();
      const product = ProductFactory.build({legend});
      const productStore: ProductStore = TestBed.get(ProductStore);
      sceneStore.set([scene]);
      productStore.set([product]);
      productStore.setActive(product.id);

      sceneService.setActive(scene.id);
      expect(legendQuery.getValue().legend).toEqual(legend);
    });
  });

  describe('get', () => {
    it('loading should be set', (done) => {
      const productId = 1;
      const product = ProductFactory.build({id: productId});
      const dateF = '2019-10-01';
      const stream = sceneQuery.selectLoading();

      stream
        .pipe(take(2), toArray())
        .subscribe(data => {
          expect(data).toEqual([true, false]);
          done();
        });

      sceneService.get(product, dateF);

      const urlParams = `date=${dateF}&timeZone=${timezone()}`;
      const url = `${environment.apiPrefixV1}/products/${product.id}/scenes?${urlParams}`;
      const request = http.expectOne(url);
      request.flush([]);
    });

    it('should call http endpoint', () => {
      const productId = 1;
      const product = ProductFactory.build({id: productId});
      const dateF = '2019-10-01';
      sceneService.get(product, dateF);

      const urlParams = `date=${dateF}&timeZone=${timezone()}`;
      const url = `${environment.apiPrefixV1}/products/${product.id}/scenes?${urlParams}`;
      const request = http.expectOne(url);
    });

    it('should set state in store', (done) => {
      const dateF = '2019-10-01';
      const product = ProductFactory.build();
      const productId = product.id;
      const productStore: ProductStore = TestBed.get(ProductStore);
      productStore.add(product);
      const scene = SceneFactory.build();

      sceneQuery
        .selectAll()
        .pipe(take(2), toArray())
        .subscribe(data => {
          expect(data).toEqual([[], [{...scene, layerName: product.layerName}]]);
          done();
        });

      sceneService.get(product, dateF);

      const urlParams = `date=${dateF}&timeZone=${timezone()}`;
      const url = `${environment.apiPrefixV1}/products/${product.id}/scenes?${urlParams}`;
      const request = http.expectOne(url);
      request.flush([scene]);
    });
  });
});
