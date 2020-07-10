import {TestBed} from '@angular/core/testing';
import {S4eConfig} from './config.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {SessionService} from '../../state/session/session.service';
import {SessionFactory} from '../../state/session/session.factory.spec';
import {of} from 'rxjs';

describe('Configuration service', () => {
  let configService: S4eConfig;
  let http: HttpTestingController;
  let sessionService: SessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [S4eConfig, SessionService],
      imports: [HttpClientTestingModule]
    });

    configService = TestBed.get(S4eConfig);
    sessionService = TestBed.get(SessionService);
    http = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(configService).toBeDefined();
  });

  it('should call endpoint', async () => {
    const spy = spyOn(sessionService, 'getProfile$').and.returnValue(of(SessionFactory.build()));
    const p = configService.loadConfiguration();

    const reqConfig = http.expectOne('api/v1/config');
    reqConfig.flush({
      geoserverUrl: 'localhost:8080/test/wms',
      geoserverWorkspace: 'testWorkspace',
      recaptchaSiteKey: 'key'
    });

    await p;

    expect(configService.geoserverUrl).toBe('localhost:8080/test/wms');
    expect(configService.geoserverWorkspace).toBe('testWorkspace');
    expect(configService.recaptchaSiteKey).toBe('key');

    http.verify();
    expect(spy).toHaveBeenCalled();
  });

});
