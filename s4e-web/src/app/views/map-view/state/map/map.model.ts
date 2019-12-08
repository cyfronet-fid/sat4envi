import {S4eConfig} from '../../../../utils/initializer/config.service';
import {InjectorModule} from '../../../../common/injector.module';

export const ZOOM_LEVELS = {
  'miasto': 10,
  'wieś': 12
};

export interface ViewPosition {
  centerCoordinates: [number, number];
  zoomLevel: number;
}

export interface MapData {
  image: string;
  width: number;
  height: number;
}

export interface MapState {
  zkOptionsOpened: boolean;
  view: ViewPosition;
}

export function createInitialState(): MapState {
  return {
    zkOptionsOpened: false,
    view: {
      centerCoordinates: InjectorModule.Injector.get(S4eConfig).projection.coordinates,
      zoomLevel: 10
    }
  };
}
