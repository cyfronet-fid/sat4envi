import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LegendService } from './legend.service';
import { LegendStore } from './legend.store';
import {RouterTestingModule} from '@angular/router/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {MapModule} from '../../map.module';

describe('LegendService', () => {
  let legendService: LegendService;
  let legendStore: LegendStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [ MapModule, RouterTestingModule, HttpClientTestingModule ]
    });

    legendService = TestBed.get(LegendService);
    legendStore = TestBed.get(LegendStore);
  });

  it('should be created', () => {
    expect(legendService).toBeDefined();
  });

});
