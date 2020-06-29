import {MapStore} from './map.store';
import {TestBed} from '@angular/core/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';
import {InjectorModule} from '../../../../common/injector.module';

describe('MapStore', () => {
  let store: MapStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TestingConfigProvider],
      imports: [MapModule]
    });

    store = TestBed.get(MapStore);
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });
});
