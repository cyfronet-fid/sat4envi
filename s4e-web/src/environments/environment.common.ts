export const commonEnvironmentVariables = {
  backendDateFormat: 'YYYY-MM-DDTHH:mm:ss[Z]',
  generalErrorKey: '__general__',
  apiPrefixV1: 'api/v1',
  projection: {toProjection: 'EPSG:3857', coordinates: [19, 52] as [number, number]},
  maxZoom: 12,
  timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
};

export default commonEnvironmentVariables;
