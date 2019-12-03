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
      centerCoordinates: [19, 52],
      zoomLevel: 10
    }
  };
}
