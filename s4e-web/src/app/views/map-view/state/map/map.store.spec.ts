import {MapStore} from './map.store';
import {TestBed} from '@angular/core/testing';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('MapStore', () => {
  let store: MapStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    store = TestBed.get(MapStore);
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });
});
