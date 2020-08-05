import { environment } from 'src/environments/environment';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import { RemoteConfiguration, ConfigurationLoader } from './config.service';
import { RemoteConfigurationFactory } from 'src/app/app.configuration.spec';

describe('Configuration service', () => {
  let http: HttpTestingController;
  let remoteConfiguration: RemoteConfiguration;
  let configurationLoader: ConfigurationLoader;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ConfigurationLoader,
        RemoteConfiguration
      ],
      imports: [HttpClientTestingModule]
    });

    remoteConfiguration = TestBed.get(RemoteConfiguration);
    configurationLoader = TestBed.get(ConfigurationLoader);
    http = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(remoteConfiguration).toBeDefined();
    expect(configurationLoader).toBeDefined();
  });

  it('loader should set configuration in remoteConfiguration', async () => {
    const spy = spyOn(remoteConfiguration, 'set');
    const configurationLoader$ = configurationLoader.load$();

    const url = `${environment.apiPrefixV1}/config`;
    const reqConfig = http.expectOne(url);
    const configuration = RemoteConfigurationFactory.build();
    reqConfig.flush(configuration);
    await configurationLoader$;

    http.verify();
    expect(spy).toHaveBeenCalledWith(configuration);
  });

});
