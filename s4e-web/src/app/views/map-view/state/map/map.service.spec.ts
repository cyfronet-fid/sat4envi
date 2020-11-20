import {LocalStorageTestingProvider, RemoteConfigurationTestingProvider} from 'src/app/app.configuration.spec';
import { InjectorModule } from './../../../../common/injector.module';
import {TestBed} from '@angular/core/testing';
import {MapService} from './map.service';
import {MapStore} from './map.store';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {ActivatedRoute, convertToParamMap, Router} from '@angular/router';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {SceneStore} from '../scene/scene.store.service';
import {SceneFactory} from '../scene/scene.factory.spec';
import {ProductFactory} from '../product/product.factory.spec';
import {OverlayStore} from '../overlay/overlay.store';
import {OverlayFactory} from '../overlay/overlay.factory.spec';
import {ActivateRoutes} from '@angular/router/src/operators/activate_routes';
import {ReplaySubject} from 'rxjs';
import {ParamMap} from '@angular/router/src/shared';
import {OverlayQuery} from '../overlay/overlay.query';
import {MapQuery} from './map.query';
import {OverlayService} from '../overlay/overlay.service';
import {ProductService} from '../product/product.service';
import {SceneService} from '../scene/scene.service';

describe('MapService', () => {
  let service: MapService;
  let store: MapStore;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RemoteConfigurationTestingProvider,
        LocalStorageTestingProvider
      ],
      imports: [
        MapModule,
        RouterTestingModule.withRoutes([]),
        InjectorModule
      ]
    });

    router = TestBed.get(Router);
    service = TestBed.get(MapService);
    store = TestBed.get(MapStore);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('connectStoreToRouter', () => {
    let spy;
    const zoomLevel = 1;
    const centerCoordinates: [number, number] = [1, 1];
    const date = '2020-01-16';
    const scene = SceneFactory.build();
    const product = ProductFactory.build();

    beforeEach(() => {
      spy = spyOn(router, 'navigate');
      TestBed.get(ProductStore).add(product);
      TestBed.get(ProductStore).setActive(product.id);
      TestBed.get(ProductStore).update(state => ({...state, ui: {...state.ui, selectedDate: date}}));
      TestBed.get(SceneStore).add(scene);
      TestBed.get(SceneStore).setActive(scene.id);
      store.update({view: {zoomLevel, centerCoordinates}});
    });


    it('should work', () => {
      service.connectStoreToRouter().subscribe();
      expect(spy).toHaveBeenCalledWith([], {
        queryParamsHandling: 'merge',
        queryParams: {
          overlays: null,
          product: product.id,
          scene: scene.id,
          date: date,
          zoom: zoomLevel,
          centerx: centerCoordinates[0],
          centery: centerCoordinates[1]
        }
      });
    });

    it('should serialize overlays', () => {
      const overlay = OverlayFactory.build();
      const overlayStore: OverlayStore = TestBed.get(OverlayStore);
      overlayStore.set([overlay]);
      overlayStore.setActive([overlay.id]);


      service.connectStoreToRouter().subscribe();
      expect(spy).toHaveBeenCalledWith([], {
        queryParamsHandling: 'merge',
        queryParams: {
          overlays: [overlay.id],
          product: product.id,
          scene: scene.id,
          date: date,
          zoom: zoomLevel,
          centerx: centerCoordinates[0],
          centery: centerCoordinates[1]
        }
      });
    });
  });

  describe('connectRouterToStore', () => {
    it('should work', async () => {
      const mapQuery: MapQuery = TestBed.get(MapQuery);
      const overlayServiceSpy = spyOn(TestBed.get(OverlayService) as OverlayService, 'setAllActive');
      const overlayStore: OverlayStore = TestBed.get(OverlayStore);
      const productServiceSpy = spyOn(TestBed.get(ProductService) as ProductService, 'setActive$');
      const productServiceSpy2 = spyOn(TestBed.get(ProductService) as ProductService, 'setSelectedDate');
      const productStore: ProductStore = TestBed.get(ProductStore);
      const sceneServiceSpy = spyOn(TestBed.get(SceneService) as SceneService, 'setActive');
      const route: ActivatedRoute = TestBed.get(ActivatedRoute);
      const queryParamMap: ReplaySubject<ParamMap> = new ReplaySubject<ParamMap>(1);
      Object.defineProperty(route, 'queryParamMap', {value: queryParamMap});

      const queryParams = {
        centerx: '5',
        centery: '5',
        zoom: '9',
        overlays: ['overlay:1'],
        product: '1',
        date: '2018-01-01',
        scene: '5'
      };

      queryParamMap.next(convertToParamMap(queryParams));
      overlayStore.setLoading(false);
      productStore.setLoading(false);

      await service.connectRouterToStore(route)
        .subscribe(() => {
          expect(mapQuery.getValue().view).toEqual({
            centerCoordinates: [Number(queryParams.centerx), Number(queryParams.centery)],
            zoomLevel: Number(queryParams.zoom)
          });

          expect(overlayServiceSpy).toBeCalledWith(queryParams.overlays);
          expect(productServiceSpy).toBeCalledWith(Number(queryParams.product));
          expect(productServiceSpy2).toBeCalledWith(queryParams.date);
          expect(sceneServiceSpy).toBeCalledWith(Number(queryParams.scene));
        })
    });

    it('should work not do anything and raise error if params are incomplete', async () => {
      const overlayStore: OverlayStore = TestBed.get(OverlayStore);
      const productStore: ProductStore = TestBed.get(ProductStore);
      const route: ActivatedRoute = TestBed.get(ActivatedRoute);
      const queryParamMap: ReplaySubject<ParamMap> = new ReplaySubject<ParamMap>(1);
      Object.defineProperty(route, 'queryParamMap', {value: queryParamMap});

      queryParamMap.next(convertToParamMap({}));
      overlayStore.setLoading(false);
      productStore.setLoading(false);

      expect(await service.connectRouterToStore(route).toPromise()).toBeFalsy();
    });
  });
});
