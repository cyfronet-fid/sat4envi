export const commonEnvironmentVariables = {
  backendDateFormat: 'YYYY-MM-DDTHH:mm:ss[Z]',
  generalErrorKey: '__general__',
  apiPrefixV1: 'api/v1',
  projection: {toProjection: 'EPSG:3857', coordinates: [19, 52] as [number, number]},
  maxZoom: 19,
  timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,

  /*
   * IMPORTANT!!! Too large frequency can provide to drastically slow down of application
   * Every nth MS UI will send request to API for latest scene
   * */
  liveSceneUpdateRateInMs: 60000
};

export default commonEnvironmentVariables;
