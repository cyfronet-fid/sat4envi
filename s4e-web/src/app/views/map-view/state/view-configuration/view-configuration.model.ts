import {ViewPosition} from '../map/map.model';

export const HC_LOCAL_STORAGE_KEY = 'highContrast';
export const LARGE_FONT_LOCAL_STORAGE_KEY = 'largeFont';

export interface ViewRouterConfig {
  overlays: number[];
  viewPosition: ViewPosition;
  productId: number;
  date: string;
  sceneId: number;
  manualDate: string|null;
}

export interface ViewConfiguration {
  caption: string;
  thumbnail: string;
  uuid?: string;
  createdAt?: string;
  configuration: ViewRouterConfig;
}

export interface ViewConfigurationEx extends ViewConfiguration {
  configurationNames: {
    product: string;
    overlays: string[];
    selectedDate: string;
  };
}
