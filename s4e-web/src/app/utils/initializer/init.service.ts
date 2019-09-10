import {Inject, Injectable, Provider} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ConfigurationResponse, IConfiguration} from '../../app.configuration';
import {deserializeJsonResponse} from '../miscellaneous/miscellaneous';
import {IConstants, S4E_CONSTANTS} from '../../app.constants';
import {map, tap} from 'rxjs/operators';

@Injectable()
export class InitService {
  private configuration: IConfiguration;

  constructor(@Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              private http: HttpClient) {
  }

  getConfiguration = (): IConfiguration => {
    return this.configuration;
  };

  loadConfiguration(): Promise<IConfiguration> {
    return this.http.get<IConfiguration>(`${this.CONSTANTS.apiPrefixV1}/config`).pipe(
      map(data => deserializeJsonResponse(data, ConfigurationResponse)),
      tap(config => {
        this.configuration = config;
      })
    ).toPromise();
  }
}
