import {MapQuery} from './map.query';
import {MapStore} from './map.store';
import {TestBed} from '@angular/core/testing';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';

describe('MapQuery', () => {
  let query: MapQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    query = TestBed.get(MapStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });
});
