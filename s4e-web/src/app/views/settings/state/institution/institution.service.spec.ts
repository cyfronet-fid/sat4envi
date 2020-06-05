import { S4eConfig } from './../../../../utils/initializer/config.service';
import { AkitaGuidService } from './../../../map-view/state/search-results/guid.service';
import { InstitutionFactory } from './institution.factory.spec';
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { InstitutionService } from './institution.service';
import { InstitutionStore } from './institution.store';
import {TestingConfigProvider} from '../../../../app.configuration.spec';
import {RouterTestingModule} from '@angular/router/testing';
import { of } from 'rxjs';
import { httpGetRequest$, httpPutRequest$, httpPostRequest$ } from 'src/app/common/store.util';
import { HttpClient } from '@angular/common/http';

describe('InstitutionService', () => {
  let institutionService: InstitutionService;
  let guidService: AkitaGuidService;
  let institutionStore: InstitutionStore;
  let s4eConfig: S4eConfig;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        InstitutionService,
        InstitutionStore,
        TestingConfigProvider,
        S4eConfig,
        AkitaGuidService
      ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule
      ],
    });

    s4eConfig = TestBed.get(S4eConfig);
    institutionService = TestBed.get(InstitutionService);
    guidService = TestBed.get(AkitaGuidService);
    institutionStore = TestBed.get(InstitutionStore);
    http = TestBed.get(HttpClient);
  });

  it('should be created', () => {
    expect(institutionService).toBeDefined();
  });

  it('should find institution', () => {
    const institution = InstitutionFactory.build();
    const spyHttpGetRequest = jasmine.createSpy('httpGetRequest$').and.returnValue(of(institution));
    const spyHttp = spyOn(http, 'get');
    const url = `${s4eConfig.apiPrefixV1}/institutions/${institution.slug}`;

    institutionService.findBy(institution.slug)
      .subscribe(loadedInstitution => {
        expect(loadedInstitution).toEqual(institution);
        expect(spyHttpGetRequest).toBeCalled();
        expect(spyHttp).toBeCalledWith(url);
      });
  });
  it('should store institutions with ids', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const id = 'test-uid';
    const spyHttp = spyOn(http, 'get').and.returnValue(of([institution]));
    const spyGuidService = spyOn(guidService, 'guid').and.returnValue(id);
    const spyStore = spyOn(institutionStore, 'set');
    const url = `${s4eConfig.apiPrefixV1}/institutions`;

    institutionService.get();
    tick();

    expect(spyGuidService).toHaveBeenCalled();
    expect(spyStore).toHaveBeenCalledWith([{...institution, id}]);
    expect(spyHttp).toHaveBeenCalledWith(url);
  }));
  it('should add new institution', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const url = `${s4eConfig.apiPrefixV1}/institutions/${institution.parentSlug}/child`;
    const spyHttp = spyOn(http, 'post').and.returnValue(of(null));

    institutionService.addInstitutionChild$(institution);
    tick();

    expect(spyHttp).toHaveBeenCalledWith(url, institution);
  }));
  it('should update institution', fakeAsync(() => {
    const institution = InstitutionFactory.build();
    const url = `${s4eConfig.apiPrefixV1}/institutions/${institution.slug}`;
    const spyHttp = spyOn(http, 'put').and.returnValue(of(null));

    institutionService.updateInstitution$(institution);
    tick();

    expect(spyHttp).toHaveBeenCalledWith(url, institution);
  }));
});
