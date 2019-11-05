import {JsonObject, JsonProperty} from 'json2typescript';

export interface IRemoteConfiguration {
  geoserverUrl: string;
  geoserverWorkspace: string;
  recaptchaSiteKey: string;
}

export interface IConfiguration extends IRemoteConfiguration {
  projection: { toProjection: string, coordinates: [number, number] };
  apiPrefixV1: string;
  userLocalStorageKey: string;
  generalErrorKey: string;
  backendDateFormat: string;
  momentDateFormat: string;
}

@JsonObject
export class RemoteConfigurationResponse implements IRemoteConfiguration {
  @JsonProperty('geoserverUrl', String)
  geoserverUrl: string = undefined;

  @JsonProperty('geoserverWorkspace', String)
  geoserverWorkspace: string = undefined;

  @JsonProperty('recaptchaSiteKey', String)
  recaptchaSiteKey: string = undefined;
}

