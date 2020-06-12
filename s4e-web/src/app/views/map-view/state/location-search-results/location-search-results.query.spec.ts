import { S4eConfig } from './../../../../utils/initializer/config.service';
import {LocationSearchResultsStore} from './locations-search-results.store';
import { LocationSearchResultsQuery } from './location-search-results.query';
import { TestBed } from '@angular/core/testing';

describe('LocationSearchResultsQuery', () => {
  let query: LocationSearchResultsQuery;

  beforeEach(() => {
    query = new LocationSearchResultsQuery(new LocationSearchResultsStore());
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });
});
