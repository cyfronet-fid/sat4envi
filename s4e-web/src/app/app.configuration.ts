import {JsonObject, JsonProperty} from 'json2typescript';

export interface IConfiguration {
  geoserverUrl: string;
  geoserverWorkspace: string;
  backendDateFormat: string;
  recaptchaSiteKey: string;
}

@JsonObject
export class ConfigurationResponse implements IConfiguration {
  @JsonProperty('geoserverUrl', String)
  geoserverUrl: string = undefined;

  @JsonProperty('geoserverWorkspace', String)
  geoserverWorkspace: string = undefined;

  @JsonProperty('backendDateFormat', String)
  backendDateFormat: string = undefined;

  @JsonProperty('recaptchaSiteKey', String)
  recaptchaSiteKey: string = undefined;
}

