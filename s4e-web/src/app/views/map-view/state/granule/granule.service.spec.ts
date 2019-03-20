import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { GranuleService } from './granule.service';
import { GranuleStore } from './granule.store';
import {TestingConstantsProvider} from '../../../../app.constants.spec';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {RecentViewStore} from '../recent-view/recent-view.store';

describe('GranuleService', () => {
  let granuleService: GranuleService;
  let granuleStore: GranuleStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        GranuleService,
        GranuleStore,
        ProductQuery,
        ProductStore,
        RecentViewQuery,
        RecentViewStore,
        TestingConstantsProvider],
      imports: [ HttpClientTestingModule ]
    });

    granuleService = TestBed.get(GranuleService);
    granuleStore = TestBed.get(GranuleStore);
  });

  it('get should work', () => {
    // :TODO add test
  });

  it('setActive should work', () => {
    // :TODO add test
  });

});
