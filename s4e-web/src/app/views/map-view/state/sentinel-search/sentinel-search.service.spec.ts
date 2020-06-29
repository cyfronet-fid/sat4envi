import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SentinelSearchService } from './sentinel-search.service';
import { SentinelSearchStore } from './sentinel-search.store';

describe('SentinelSearchResultService', () => {
  let sentinelSearchResultService: SentinelSearchService;
  let sentinelSearchResultStore: SentinelSearchStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SentinelSearchService, SentinelSearchStore],
      imports: [ HttpClientTestingModule ]
    });

    sentinelSearchResultService = TestBed.get(SentinelSearchService);
    sentinelSearchResultStore = TestBed.get(SentinelSearchStore);
  });

  it('should be created', () => {
    expect(sentinelSearchResultService).toBeDefined();
  });

});
