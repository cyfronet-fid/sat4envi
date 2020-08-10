import {OverlayQuery} from './overlay.query';
import {OverlayStore} from './overlay.store';
import {async, TestBed} from '@angular/core/testing';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import { RemoteConfigurationTestingProvider } from 'src/app/app.configuration.spec';

describe('OverlayQuery', () => {
  let query: OverlayQuery;
  let store: OverlayStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
      providers: [RemoteConfigurationTestingProvider]
    });

    query = TestBed.get(OverlayQuery);
    store = TestBed.get(OverlayStore);
  }));

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  describe('selectAllAsUIOverlays', () => {
    it('should work', (done) => {
      store.add({caption: 'test', type: 'wms', id: 'test-overlay-id'});
      query.selectAllAsUIOverlays().subscribe(overlays => {

        expect(overlays.length).toEqual(1);

        expect(overlays[0].olLayer.values_.source.url_).toEqual('http://localhost:8080/geoserver/wms');
        expect(overlays[0].olLayer.values_.source.params_.LAYERS).toEqual('test-overlay-id');

        expect(overlays.map(o => ({...o, olLayer: undefined}))).toEqual([
          {
            id: 'test-overlay-id',
            caption: 'test',
            type: 'wms',
            cid: 'test-overlay-id',
            active: false,
            favourite: false,
            isFavouriteLoading: false,
            isLoading: false
          }
        ]);
        done();
      });
    });
  });
});
