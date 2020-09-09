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

describe('ViewConfigurationQuery', () => {
  let query: ViewConfigurationQuery;
  let store: ViewConfigurationStore;
  let mapQuery: MapQuery;
  let productQuery: ProductQuery;
  let sceneQuery: SceneQuery;
  let overlayQuery: OverlayQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    query = TestBed.get(ViewConfigurationQuery);
    store = TestBed.get(ViewConfigurationStore);
    productQuery = TestBed.get(ProductQuery);
    overlayQuery = TestBed.get(OverlayQuery);
    mapQuery = TestBed.get(MapQuery);
    sceneQuery = TestBed.get(SceneQuery);
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
    let productSpyActive = jest.spyOn(productQuery, 'getActiveId');
    productSpyActive.mockReturnValueOnce(10);
    let productSpy = jest.spyOn(productQuery, 'getEntity');
    productSpy.mockReturnValueOnce({
      description: '', imageUrl: '', legend: undefined,
      id: 10,
      name: 'product 10 name'
    });
    let productSpyDate = jest.spyOn(productQuery, 'getValue');
    productSpyDate.mockReturnValueOnce(
      {
        active: undefined,
        ui: {availableDays: [], loadedMonths: [], selectedDate: '11-11-2020', selectedDay: 0, selectedMonth: 0, selectedYear: 0}
      }
    );
    let overlaySpyActive = jest.spyOn(overlayQuery, 'getActive');
    overlaySpyActive.mockReturnValueOnce(
      [
        {
          label: 'overlay 1 caption', id: 'ov1', type: undefined
        },
        {
          label: 'overlay 3 caption', id: 'ov3', type: undefined
        }
      ]
    );
    let overlaySpy = jest.spyOn(overlayQuery, 'getEntity');
    overlaySpy.mockReturnValueOnce({
      label: 'overlay 1 caption', id: 'ov1', type: undefined
    }).mockReturnValueOnce({
      label: 'overlay 3 caption', id: 'ov31', type: undefined
    });

    let sceneSpyActive = jest.spyOn(sceneQuery, 'getActiveId');
    sceneSpyActive.mockReturnValueOnce(78);

    let mapSpy = jest.spyOn(mapQuery, 'getValue');
    mapSpy.mockReturnValueOnce(
      {
        zkOptionsOpened: false,
        loginOptionsOpened: false,
        view: {
          zoomLevel: 9,
          centerCoordinates: [56, 67]
        }
      }
    );

    const viewConfigEx: ViewConfigurationEx = {
      caption: '',
      configuration: {
        overlays: ['ov1', 'ov3'],
        productId: 10,
        sceneId: 78,
        date: '11-11-2020',
        viewPosition: {
          zoomLevel: 9,
          centerCoordinates: [56, 67]
        }
      },
      configurationNames: {overlays: ['overlay 1 caption', 'overlay 3 caption'], product: 'product 10 name', selectedDate: '11-11-2020'},
      thumbnail: null
    };

    expect(query.getCurrent()).toEqual(viewConfigEx);
    expect(productSpyActive).toHaveBeenCalled();
    expect(productSpy).toHaveBeenCalledWith(10);
    expect(productSpyDate).toHaveBeenCalled();
    expect(overlaySpyActive).toHaveBeenCalled();
    expect(overlaySpy.mock.calls.length).toBe(2);
    expect(sceneSpyActive).toHaveBeenCalled();
    expect(mapSpy).toHaveBeenCalled();
  });

});
