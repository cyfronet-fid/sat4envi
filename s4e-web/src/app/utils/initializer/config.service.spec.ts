import {TestBed} from '@angular/core/testing';
import {S4eConfig} from './config.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {IConfiguration} from '../../app.configuration';

describe('Configuration service', () => {
  let configService: S4eConfig;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [S4eConfig],
      imports: [HttpClientTestingModule]
    });

    configService = TestBed.get(S4eConfig);
    http = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(configService).toBeDefined();
  });

  it('should call endpoint', (done) => {
    configService.loadConfiguration().then(() => {
      expect(configService.geoserverUrl).toBe('localhost:8080/test/wms');
      expect(configService.geoserverWorkspace).toBe('testWorkspace');
      expect(configService.recaptchaSiteKey).toBe('key');
      done();
    });

    const req = http.expectOne('api/v1/config');

    req.flush({
      geoserverUrl: 'localhost:8080/test/wms',
      geoserverWorkspace: 'testWorkspace',
      recaptchaSiteKey: 'key'
    });


    http.verify();
  });

});
