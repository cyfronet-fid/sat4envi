import { InstitutionsSearchResultsStore } from './institutions-search-results.store';
import { InstitutionsSearchResultsQuery } from './institutions-search-results.query';
import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {InjectorModule} from '../../../../common/injector.module';
import {RouterTestingModule} from '@angular/router/testing';
import { InstitutionsSearchResultsService } from './institutions-search-results.service';
import { AkitaGuidService } from 'src/app/views/map-view/state/search-results/guid.service';
import { InstitutionQuery } from '../institution/institution.query';
import { of } from 'rxjs';

describe('InstitutionsSearchResultsService', () => {
  let institutionSearchResultsService: InstitutionsSearchResultsService;
  let institutionSearchResultsStore: InstitutionsSearchResultsStore;
  let institutionSearchResultsQuery: InstitutionsSearchResultsQuery;
  let institutionQuery: InstitutionQuery;
  let http: HttpTestingController;
  let guidService: AkitaGuidService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        InstitutionsSearchResultsService,
        InstitutionsSearchResultsQuery,
        InstitutionsSearchResultsStore,
        TestingConfigProvider,
        AkitaGuidService
      ],
      imports: [
        InjectorModule,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    });

    http = TestBed.get(HttpTestingController);
    institutionSearchResultsService = TestBed.get(InstitutionsSearchResultsService);
    institutionSearchResultsStore = TestBed.get(InstitutionsSearchResultsStore);
    institutionSearchResultsQuery = TestBed.get(InstitutionsSearchResultsQuery);
    institutionQuery = TestBed.get(InstitutionQuery);
    guidService = TestBed.get(AkitaGuidService);
  });

  it('should be created', () => {
    expect(institutionSearchResultsService).toBeDefined();
  });

  describe('get', () => {
    it('should correctly handle empty query string', () => {
      institutionSearchResultsService.get('');

      expect(institutionSearchResultsQuery.getValue().isOpen).toEqual(true);
      expect(institutionSearchResultsQuery.getValue().queryString).toEqual('');
    });

    it('should handle non-empty query', fakeAsync(() => {
      const sampleResult = {
        id: '1234-5678',
        name: 'zarządzaj#1',
        slug: 'zarządzaj-1'
      };
      spyOn(institutionQuery, 'selectAll').and.returnValue(of([sampleResult]));
      institutionSearchResultsService.get('zarz');
      tick(1000);

      expect(institutionSearchResultsQuery.getValue().isOpen).toEqual(true);
      expect(institutionSearchResultsQuery.getValue().queryString).toEqual('zarz');
      expect(institutionSearchResultsQuery.getAll()).toEqual([sampleResult]);
    }));

  });
});
