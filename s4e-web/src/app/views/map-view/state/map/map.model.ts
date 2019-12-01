// tslint:disable-next-line:no-empty-interface
export interface ViewPosition {
  centerCoordinates: [number, number];
  zoomLevel: number;
}

export interface MapState {
  zkOptionsOpened: boolean;
  view: ViewPosition;
}

export function createInitialState(): MapState {
  return {
    zkOptionsOpened: false,
    view: {
      centerCoordinates: [19, 52],
      zoomLevel: 10
    }
  };
}

export const ZOOM_LEVELS = {
  'miasto': 10,
  'wieś': 12
};
