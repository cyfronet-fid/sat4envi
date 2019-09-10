import {InjectionToken, Provider} from '@angular/core';

export interface IConstants {
  projection: {toProjection: string, coordinates: [number, number]};
  apiPrefixV1: string;
  userLocalStorageKey: string;
  generalErrorKey: string;
}

export function s4eConstantsFactory(): IConstants {
  return {
    projection: {toProjection: 'EPSG:3857', coordinates: [19, 52]},
    apiPrefixV1: 'api/v1',
    userLocalStorageKey: 'user',
    generalErrorKey: '__general__'
  };
}


export const S4E_CONSTANTS = new InjectionToken<IConstants>('S4E_CONSTANTS');

export const ConstantsProvider: Provider = {provide: S4E_CONSTANTS, useValue: s4eConstantsFactory()};
