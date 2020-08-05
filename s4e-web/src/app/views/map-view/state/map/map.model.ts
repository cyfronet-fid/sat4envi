import environment from 'src/environments/environment';

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
  loginOptionsOpened: boolean;
  productDescriptionOpened: boolean;

  view: ViewPosition;
}

export function createInitialState(): MapState {
  return {
    zkOptionsOpened: false,
    loginOptionsOpened: false,
    productDescriptionOpened: false,
    view: {
      centerCoordinates: environment.projection.coordinates,
      zoomLevel: 10
    }
  };
}
