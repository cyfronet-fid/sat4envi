import {ImageWMS} from 'ol/source';
import {Image, Layer} from 'ol/layer';
import {IUILayer} from '../common.model';
import {EntityState} from '@datorama/akita';

// Overlay
// IUILayer -> caption to label

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

export function convertToUIOverlay(overlay: Overlay, geoServerUrl: string, active: boolean = false): UIOverlay {
  return {
    favourite: false,
    ...overlay,
    olLayer: new Image({
      source: new ImageWMS({
        crossOrigin: 'Anonymous',
        url: overlay.url,
        serverType: 'geoserver',
        params: {LAYERS: overlay.id}
      })
    }),
    cid: overlay.id,
    active: active,
    isLoading: false,
    isFavouriteLoading: false
  };
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
