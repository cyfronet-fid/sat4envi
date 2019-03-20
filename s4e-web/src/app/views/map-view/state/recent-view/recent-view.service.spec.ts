import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RecentViewService } from './recent-view.service';
import { RecentViewStore } from './recent-view.store';
import {RecentViewQuery} from './recent-view.query';
import {GranuleService} from '../granule/granule.service';
import {TestingConstantsProvider} from '../../../../app.constants.spec';

describe('RecentViewService', () => {
  let recentViewService: RecentViewService;
  let recentViewStore: RecentViewStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RecentViewService,
        RecentViewStore,
        HttpClientTestingModule,
        RecentViewQuery,
        GranuleService,
        TestingConstantsProvider
      ],
      imports: [ HttpClientTestingModule ]
    });

    recentViewService = TestBed.get(RecentViewService);
    recentViewStore = TestBed.get(RecentViewStore);
  });

  it('should be created', () => {
    expect(recentViewService).toBeDefined();
  });

});
