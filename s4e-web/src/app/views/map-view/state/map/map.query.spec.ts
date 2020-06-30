import {MapQuery} from './map.query';
import {MapStore} from './map.store';
import {TestBed} from '@angular/core/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('MapQuery', () => {
  let query: MapQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
      providers: [TestingConfigProvider]
    });

    query = TestBed.get(MapStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });
});
