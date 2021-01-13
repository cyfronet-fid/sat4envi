/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import environment from 'src/environments/environment';

export const SIDEBAR_OPEN_LOCAL_STORAGE_KEY = 'sidebarOpen';
export const PRODUCT_DESCRIPTION_CLOSED_LOCAL_STORAGE_KEY = 'productDescriptionClosed';

export interface ViewPosition {
  centerCoordinates: [number, number];
  zoomLevel: number;
}

export interface MapData {
  image: string;
  width: number;
  height: number;
  pointResolution: number;
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
    productDescriptionOpened: storage.getItem(PRODUCT_DESCRIPTION_CLOSED_LOCAL_STORAGE_KEY) === null
      ? true : !JSON.parse(storage.getItem(PRODUCT_DESCRIPTION_CLOSED_LOCAL_STORAGE_KEY)),
    sidebarOpen: storage.getItem(SIDEBAR_OPEN_LOCAL_STORAGE_KEY) === null
      ? true : JSON.parse(storage.getItem(SIDEBAR_OPEN_LOCAL_STORAGE_KEY)),
    view: {
      centerCoordinates: environment.projection.coordinates,
      zoomLevel: 10
    }
  };
}
