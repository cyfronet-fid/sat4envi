import { OverlayQuery } from './overlay.query';
import { OverlayStore } from './overlay.store';
import {async, TestBed} from '@angular/core/testing';
import {TestingConstantsProvider} from '../../../../app.constants.spec';
import {TileWMS, ImageWMS, OSM} from 'ol/source';
import {Tile, Image, Layer} from 'ol/layer';

describe('OverlayQuery', () => {
  let query: OverlayQuery;
  let store: OverlayStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [OverlayStore, TestingConstantsProvider]
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
            active: true
          }
        ]);
        done();
      });
    });
  });
});
