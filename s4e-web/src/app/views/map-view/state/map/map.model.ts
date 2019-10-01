// tslint:disable-next-line:no-empty-interface
export interface MapState {
  legendOpened: boolean;
}

export function createInitialState(): MapState {
  return {
    legendOpened: false
  };
}
