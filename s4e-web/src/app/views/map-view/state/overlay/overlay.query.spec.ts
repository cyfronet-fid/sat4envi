import { GLOBAL_OWNER_TYPE } from './overlay.model';
import {OverlayQuery} from './overlay.query';
import {OverlayStore} from './overlay.store';
import {async, TestBed} from '@angular/core/testing';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {RemoteConfigurationTestingProvider} from 'src/app/app.configuration.spec';
import {OverlayFactory} from './overlay.factory.spec';

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

  describe('selectVisibleAsUIOverlays', () => {
    it('should work', (done) => {
      store.add(OverlayFactory.build({label: 'test', ownerType: GLOBAL_OWNER_TYPE, id: 'test-overlay-id'}));
      query.selectVisibleAsUIOverlays().subscribe(overlays => {

        expect(overlays.length).toEqual(1);

        expect((overlays[0].olLayer as any).values_.source.url_).toEqual('http://localhost:8080/geoserver/wms');
        expect((overlays[0].olLayer as any).values_.source.params_.LAYERS).toEqual('test-overlay-id');

        expect(overlays.map(o => ({...o, olLayer: undefined}))).toEqual([
          {
            id: 'test-overlay-id',
            label: 'test',
            ownerType: GLOBAL_OWNER_TYPE,
            cid: 'test-overlay-id',
            active: false,
            favourite: false,
            isFavouriteLoading: false,
            isLoading: false,
            visible: true,
            url: 'http://localhost:8080/geoserver/wms',
            createdAt: null,
            olLayer: undefined
          }
        ]);
        done();
      });
    });
  });
});
