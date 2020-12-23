/*
 * Copyright 2020 ACC Cyfronet AGH
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

import {Layer, Tile} from 'ol/layer';
import {IUILayer} from '../common.model';
import {EntityState} from '@datorama/akita';

export const GLOBAL_OWNER_TYPE = 'GLOBAL';
export const INSTITUTIONAL_OWNER_TYPE = 'INSTITUTIONAL';
export const PERSONAL_OWNER_TYPE = 'PERSONAL';
export type OwnerType = typeof GLOBAL_OWNER_TYPE | typeof PERSONAL_OWNER_TYPE | typeof INSTITUTIONAL_OWNER_TYPE;

export interface Overlay {
  id: number;
  ownerType: OwnerType;
  institutionSlug?: string;
  url: string;
  label: string;
  visible: boolean;
  createdAt: string|null;
}

/**
 * This is transient Overlay object which can be returned by some
 * queries
 */
export interface UIOverlay extends Overlay, IUILayer {
  olLayer: Layer;
}

export interface OverlayUI {
  loadingVisible: boolean;
  loadingDelete: boolean;
  loadingPublic: boolean;
}

export interface OverlayUIState extends EntityState<OverlayUI> {
  showNewOverlayForm: boolean;
  loadingNew: boolean;
}
