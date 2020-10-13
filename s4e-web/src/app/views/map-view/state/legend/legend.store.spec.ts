import { LegendStore } from './legend.store';
import {TestBed} from '@angular/core/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('LegendStore', () => {
  let store: LegendStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [ MapModule, RouterTestingModule, HttpClientTestingModule ]
    });

    store = TestBed.get(LegendStore);
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });
});
