import {TileWMS, ImageWMS, OSM} from 'ol/source';
import {Tile, Image, Layer} from 'ol/layer';
import {IUILayer} from '../common.model';

export type OverlayType = 'wms'; // later |'tile'|'etc' may be added

export interface Overlay {
  id: string;
  type: OverlayType;
  caption: string;
}

/**
 * A factory function that creates Overlay
 */
export function createOverlay(params: Partial<Overlay>) {
  return {

  } as Overlay;
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
    ...overlay,
    olLayer: new Image({
      source: new ImageWMS({
        url: geoServerUrl,
        params: {LAYERS: overlay.id}
      })
    }),
    cid: overlay.id,
    active: true
  };
}
