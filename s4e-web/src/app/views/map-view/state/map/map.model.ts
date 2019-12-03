// tslint:disable-next-line:no-empty-interface
export interface MapState {
  zkOptionsOpened: boolean
}

export function createInitialState(): MapState {
  return {
    zkOptionsOpened: false
  };
}
