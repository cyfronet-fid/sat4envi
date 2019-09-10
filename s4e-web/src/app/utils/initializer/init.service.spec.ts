import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {InitService} from './init.service';
import {TestingConstantsProvider} from '../../app.constants.spec';

describe('Initalization service', () => {
  let initService: InitService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TestingConstantsProvider, InitService],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    initService = TestBed.get(InitService);
  });

  it('should be created', () => {
    expect(initService).toBeDefined();
  });

  it('should call endpoint', () => {
    initService.loadConfiguration();
    const req = http.expectOne('api/v1/config');

    req.flush({
      geoserverUrl: 'localhost:8080/test/wms',
      geoserverWorkspace: 'testWorkspace',
      backendDateFormat: 'yyyy-mm-dd',
      recaptchaSiteKey: 'key'
    });

    http.verify();
  });

  it('should store configuration', () => {
    initService.loadConfiguration();
    const req = http.expectOne('api/v1/config');

    req.flush({
      geoserverUrl: 'localhost:8080/test/wms',
      geoserverWorkspace: 'testWorkspace',
      backendDateFormat: 'yyyy-mm-dd',
      recaptchaSiteKey: 'key'
    });

    const config = initService.getConfiguration();
    expect(config).toEqual({
      geoserverUrl: 'localhost:8080/test/wms',
      geoserverWorkspace: 'testWorkspace',
      backendDateFormat: 'yyyy-mm-dd',
      recaptchaSiteKey: 'key'
    });
    http.verify();
  });
});
