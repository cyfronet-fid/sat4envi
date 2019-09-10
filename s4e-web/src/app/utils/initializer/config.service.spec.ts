import {TestBed} from '@angular/core/testing';
import {InitService} from './init.service';
import {S4eConfig} from './config.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {IConfiguration} from '../../app.configuration';

class MockInitService {
  getConfiguration(): IConfiguration {
    return {
      geoserverUrl: 'http://localhost:8080/test/wms',
      geoserverWorkspace: 'testing',
      backendDateFormat: 'yyyy-MM-dd',
      recaptchaSiteKey: 'abc123'
    };
  }
}

describe('Configuration service', () => {
  let configService: S4eConfig;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {provide: InitService, useClass: MockInitService},
        S4eConfig],
      imports: [HttpClientTestingModule]
    });

    configService = TestBed.get(S4eConfig);
  });

  it('should be created', () => {
    expect(configService).toBeDefined();
  });

  it('should call endpoint', () => {
    expect(configService.geoserverUrl).toBe('http://localhost:8080/test/wms');
    expect(configService.geoserverWorkspace).toBe('testing');
    expect(configService.backendDateFormat).toBe('yyyy-MM-dd');
    expect(configService.recaptchaSiteKey).toBe('abc123');
  });

});
