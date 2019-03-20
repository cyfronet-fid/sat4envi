import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MapService } from './map.service';
import { MapStore } from './map.store';
import {TestingConstantsProvider} from '../../../../app.constants.spec';

describe('MapService', () => {
  let mapService: MapService;
  let mapStore: MapStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MapService, MapStore, TestingConstantsProvider],
      imports: [ HttpClientTestingModule ]
    });

    mapService = TestBed.get(MapService);
    mapStore = TestBed.get(MapStore);
  });

  it('should be created', () => {
    expect(mapService).toBeDefined();
  });

});
