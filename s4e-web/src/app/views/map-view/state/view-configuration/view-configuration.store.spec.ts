import { ViewConfigurationStore } from './view-configuration.store';
import {TestBed} from '@angular/core/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';

describe('ViewConfigurationStore', () => {
  let store: ViewConfigurationStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    store = TestBed.get(ViewConfigurationStore);
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
