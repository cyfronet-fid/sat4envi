import {Layer, Tile} from 'ol/layer';
import {IUILayer} from '../common.model';
import {EntityState} from '@datorama/akita';
import { getBaseUrlAndParamsFrom } from '../../view-manager/overlay-list-modal/wms-url.utils';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import {TileLoader} from '../utils/layers-loader.util';

export const GLOBAL_OWNER_TYPE = 'GLOBAL';
export const INSTITUTIONAL_OWNER_TYPE = 'INSTITUTIONAL';
export const PERSONAL_OWNER_TYPE = 'PERSONAL';
export type OwnerType = typeof GLOBAL_OWNER_TYPE | typeof PERSONAL_OWNER_TYPE | typeof INSTITUTIONAL_OWNER_TYPE;

export interface Overlay {
  id: string;
  ownerType: OwnerType;
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
