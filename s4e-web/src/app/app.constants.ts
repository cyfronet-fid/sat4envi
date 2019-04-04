import {InjectionToken, Provider} from '@angular/core';

export interface IConstants {
  projection: {toProjection: string, coordinates: [number, number]};
  geoserverUrl: string;
  geoserverWorkspace: string;
  apiPrefixV1: string;
  backendDateFormat: string;
  userLocalStorageKey: string;
}

export function s4eConstantsFactory(): IConstants {
  return {
    projection: {toProjection: 'EPSG:3857', coordinates: [19, 52]},
    geoserverUrl: 'http://localhost:8080/geoserver/wms',
    geoserverWorkspace: 'development',
    apiPrefixV1: 'api/v1',
    backendDateFormat: 'yyyy-MM-dd\'T\'HH:mm:ss',
    userLocalStorageKey: 'user'
  };
}


export const S4E_CONSTANTS = new InjectionToken<IConstants>('S4E_CONSTANTS');

export const ConstantsProvider: Provider = {provide: S4E_CONSTANTS, useValue: s4eConstantsFactory()};
