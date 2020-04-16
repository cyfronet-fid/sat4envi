import {ImageWMS} from 'ol/source';
import {Image, Layer} from 'ol/layer';
import {IUILayer} from '../common.model';

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
}

/**
 * A factory function that creates Overlay
 */
export function createOverlay(params: Partial<Overlay>) {
  return {} as Overlay;
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
        url: geoServerUrl,
        serverType: 'geoserver',
        params: {LAYERS: overlay.id}
      })
    }),
    cid: overlay.id,
    active: active
  };
}
