import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LegendService } from './legend.service';
import { LegendStore } from './legend.store';

describe('LegendService', () => {
  let legendService: LegendService;
  let legendStore: LegendStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LegendService, LegendStore],
      imports: [ HttpClientTestingModule ]
    });

    legendService = TestBed.get(LegendService);
    legendStore = TestBed.get(LegendStore);
  });

  it('should be created', () => {
    expect(legendService).toBeDefined();
  });

});
