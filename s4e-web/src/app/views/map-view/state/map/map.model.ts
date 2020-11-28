import environment from 'src/environments/environment';

export const SIDEBAR_OPEN_LOCAL_STORAGE_KEY = 'sidebarOpen';

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
  sidebarOpen: boolean;
  view: ViewPosition;
}

export function createInitialState(storage: Storage): MapState {
  return {
    zkOptionsOpened: false,
    loginOptionsOpened: false,
    productDescriptionOpened: false,
    sidebarOpen: storage.getItem(SIDEBAR_OPEN_LOCAL_STORAGE_KEY) === null ? true : JSON.parse(storage.getItem('sidebarOpen')),
    view: {
      centerCoordinates: environment.projection.coordinates,
      zoomLevel: 10
    }
  };
}
