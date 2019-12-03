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
