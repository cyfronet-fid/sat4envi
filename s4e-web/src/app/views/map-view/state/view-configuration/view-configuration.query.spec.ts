import {ViewConfigurationQuery} from './view-configuration.query';
import {ViewConfigurationStore} from './view-configuration.store';
import {TestBed} from '@angular/core/testing';
import {MapQuery} from '../map/map.query';
import {ProductQuery} from '../product/product.query';
import {SceneQuery} from '../scene/scene.query';
import {OverlayQuery} from '../overlay/overlay.query';
import {ViewConfiguration, ViewConfigurationEx} from './view-configuration.model';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {OverlayFactory} from '../overlay/overlay.factory.spec';
import {ProductStore} from '../product/product.store';
import {OverlayStore} from '../overlay/overlay.store';
import {ProductFactory} from '../product/product.factory.spec';
import {SceneStore} from '../scene/scene.store.service';
import {SceneFactory} from '../scene/scene.factory.spec';
import {MapStore} from '../map/map.store';

describe('ViewConfigurationQuery', () => {
  let query: ViewConfigurationQuery;
  let store: ViewConfigurationStore;
  let mapQuery: MapQuery;
  let mapStore: MapStore;
  let productQuery: ProductQuery;
  let sceneQuery: SceneQuery;
  let overlayQuery: OverlayQuery;
  let productStore: ProductStore;
  let overlayStore: OverlayStore;
  let sceneStore: SceneStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    query = TestBed.get(ViewConfigurationQuery);
    store = TestBed.get(ViewConfigurationStore);
    productStore = TestBed.get(ProductStore);
    productQuery = TestBed.get(ProductQuery);
    overlayQuery = TestBed.get(OverlayQuery);
    overlayStore = TestBed.get(OverlayStore);
    mapQuery = TestBed.get(MapQuery);
    sceneQuery = TestBed.get(SceneQuery);
    sceneStore = TestBed.get(SceneStore);
    mapStore = TestBed.get(MapStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('mapToExtended should work', () => {
    const viewConfig: ViewConfiguration = {
      caption: 'dummyCaption',
      configuration: {
        overlays: ['ov1'],
        productId: 1,
        sceneId: 23,
        date: '01-01-2020',
        viewPosition: {
          zoomLevel: 10,
          centerCoordinates: [100, 200]
        }
      },
      thumbnail: 'base64thumbnail',
      uuid: 'dummyUuid'
    };

    let productSpy = jest.spyOn(productQuery, 'getEntity');
    productSpy.mockReturnValueOnce({
      description: '',
      imageUrl: '',
      legend: undefined,
      id: 1,
      name: 'product 1 name'
    });
    let overlaySpy = jest.spyOn(overlayQuery, 'getEntity');
    overlaySpy.mockReturnValueOnce({
      label: 'overlay 1 caption', id: 'ov1', type: undefined
    });


    const viewConfigEx: ViewConfigurationEx = {
      caption: 'dummyCaption',
      configuration: {
        overlays: ['ov1'],
        productId: 1,
        sceneId: 23,
        date: '01-01-2020',
        viewPosition: {
          zoomLevel: 10,
          centerCoordinates: [100, 200]
        }
      },
      configurationNames: {overlays: ['overlay 1 caption'], product: 'product 1 name', selectedDate: '01-01-2020'},
      thumbnail: 'base64thumbnail',
      uuid: 'dummyUuid'
    };

    expect(query.mapToExtended(viewConfig)).toEqual(viewConfigEx);
    expect(productSpy).toHaveBeenCalledWith(1);
    expect(overlaySpy).toHaveBeenCalledWith('ov1');
  });

  it('getCurrent should work', () => {
    const product = ProductFactory.build();
    productStore.set([product]);
    productStore.setActive(product.id);
    productStore.update(state => (
      {...state, ui: {...state.ui, selectedMonth: 11, selectedYear: 2020, selectedDay: 11, selectedDate: '11-11-2020'}}
    ));

    const overlays = OverlayFactory.buildList(2);
    overlayStore.set(overlays);
    overlayStore.setActive(overlays.map(ol => ol.id));

    const scene = SceneFactory.build();
    sceneStore.set([scene]);
    sceneStore.setActive(scene.id);

    mapStore.update({
      sidebarOpen: false,
      productDescriptionOpened: false,
      zkOptionsOpened: false,
      loginOptionsOpened: false,
      view: {
        zoomLevel: 9,
        centerCoordinates: [56, 67]
      }
    });

    const viewConfigEx: ViewConfigurationEx = {
      caption: '',
      configuration: {
        overlays: [overlays[0].id, overlays[1].id],
        productId: product.id,
        sceneId: scene.id,
        date: '11-11-2020',
        viewPosition: {
          zoomLevel: 9,
          centerCoordinates: [56, 67]
        }
      },
      configurationNames: {overlays: overlays.map(ol => ol.label), product: product.displayName, selectedDate: '11-11-2020'},
      thumbnail: null
    };

    expect(query.getCurrent()).toEqual(viewConfigEx);
  });
});
