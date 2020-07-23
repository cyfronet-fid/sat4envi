import {ImageWMS} from 'ol/source';
import {Image, Layer} from 'ol/layer';
import {IUILayer} from '../common.model';
import {EntityState} from '@datorama/akita';

export type OverlayType = 'wms'; // later |'tile'|'etc' may be added

export interface OverlayResponse {
  id: number;
  name: string;
  layerName: string; // id
}

export interface Overlay {
  id: string;
  type: OverlayType;
  caption: string;
  // :TODO: below fields have not been added to the API yet, they might need to be renamed later on
  url: string;
  layerName: string;
  mine: boolean;
  visible: boolean;
  created: string|null; //date string
}

/**
 * A factory function that creates Overlay
 */
export function createOverlay(params: Partial<Overlay> & {id: string, type: OverlayType, caption: string, layerName: string}): Overlay {
  return {
    mine: false,
    visible: true,
    created: new Date().toISOString(),
    url: '',
    ...params
  }
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
