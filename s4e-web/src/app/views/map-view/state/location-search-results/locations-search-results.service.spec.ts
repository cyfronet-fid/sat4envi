import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {SearchResultsService} from './locations-search-results.service';
import {LocationSearchResultsStore} from './locations-search-results.store';
import {RouterTestingModule} from '@angular/router/testing';
import { AkitaGuidService } from '../search-results/guid.service';
import { LocationSearchResultsQuery } from './location-search-results.query';
import environment from 'src/environments/environment';


describe('SearchResultsService', () => {
  let searchResultsService: SearchResultsService;
  let searchResultsStore: LocationSearchResultsStore;
  let searchResultsQuery: LocationSearchResultsQuery;
  let http: HttpTestingController;
  let guidService: AkitaGuidService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SearchResultsService, LocationSearchResultsStore, LocationSearchResultsQuery, AkitaGuidService],
      imports: [HttpClientTestingModule, RouterTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    searchResultsService = TestBed.get(SearchResultsService);
    searchResultsStore = TestBed.get(LocationSearchResultsStore);
    searchResultsQuery = TestBed.get(LocationSearchResultsQuery);
    guidService = TestBed.get(AkitaGuidService);
  });

  it('should be created', () => {
    expect(searchResultsService).toBeDefined();
  });

  describe('get', () => {
    it('should correctly handle empty query string', () => {
      searchResultsService.get('');

      expect(searchResultsQuery.getValue().isOpen).toEqual(false);
      expect(searchResultsQuery.getValue().queryString).toEqual('');

      http.verify();
    });

    it('should pass query to endpoint', () => {
      searchResultsService.get('foo');
      const req = http.expectOne(`${environment.apiPrefixV1}/places?namePrefix=foo`);
      expect(req.request.method).toBe('GET');
      req.flush({});
      http.verify();
    });

    it('should handle non-empty query', fakeAsync(() => {
      const sampleId = '1234-5678';
      jest.spyOn(guidService, 'guid').mockReturnValueOnce(sampleId);
      searchResultsService.get('k');
      const req = http.expectOne(`${environment.apiPrefixV1}/places?namePrefix=k`);
      expect(req.request.method).toBe('GET');

      const sampleResult = {
        latitude: 12,
        longitude: 23,
        name: 'kraków',
        type: 'miasto',
        voivodeship: 'małopolska'
      };
      req.flush({
        content: [sampleResult]
      });
      tick(1000);

      const expectedResult = {...sampleResult, id: sampleId};
      expect(searchResultsQuery.getValue().isOpen).toEqual(true);
      expect(searchResultsQuery.getValue().queryString).toEqual('k');
      expect(searchResultsQuery.getAll()).toEqual([expectedResult]);
    }));

  });
});
