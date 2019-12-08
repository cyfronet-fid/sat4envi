import { MapQuery } from './map.query';
import { MapStore } from './map.store';
import {TestBed} from '@angular/core/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';
import {InjectorModule} from '../../../../common/injector.module';

describe('MapQuery', () => {
  let query: MapQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TestingConfigProvider],
      imports: [MapModule]
    });

    query = TestBed.get(MapStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });
});
