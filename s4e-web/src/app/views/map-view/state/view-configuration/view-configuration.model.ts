import {ViewPosition} from '../map/map.model';
import {ViewConfigurationState} from './view-configuration.store';

export interface ViewRouterConfig {
  overlays: number[];
  viewPosition: ViewPosition;
  productId: number;
  date: string;
  sceneId: number;
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

/**
 * A factory function that creates ViewConfiguration
 */
export function createViewConfiguration(params: Partial<ViewConfigurationState> = {}): ViewConfigurationState {
  return {
    loading: false
  } as ViewConfigurationState;
}
