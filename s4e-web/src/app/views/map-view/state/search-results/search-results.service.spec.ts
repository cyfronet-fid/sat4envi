import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {SearchResultsService} from './search-results.service';
import {SearchResultsStore} from './search-results.store';
import {SearchResultsQuery} from './search-results.query';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {AkitaGuidService} from './guid.service';


describe('SearchResultsService', () => {
  let searchResultsService: SearchResultsService;
  let searchResultsStore: SearchResultsStore;
  let searchResultsQuery: SearchResultsQuery;
  let http: HttpTestingController;
  let guidService: AkitaGuidService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SearchResultsService, SearchResultsStore, SearchResultsQuery, TestingConfigProvider, AkitaGuidService],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    searchResultsService = TestBed.get(SearchResultsService);
    searchResultsStore = TestBed.get(SearchResultsStore);
    searchResultsQuery = TestBed.get(SearchResultsQuery);
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
      const req = http.expectOne('api/v1/places?namePrefix=foo');
      expect(req.request.method).toBe('GET');
      req.flush({});
      http.verify();
    });

    it('should handle non-empty query', fakeAsync(() => {
      const sampleId = '1234-5678';
      jest.spyOn(guidService, 'guid').mockReturnValueOnce(sampleId);
      searchResultsService.get('k');
      const req = http.expectOne('api/v1/places?namePrefix=k');
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
