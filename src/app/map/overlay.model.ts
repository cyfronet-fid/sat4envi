import {Layer} from 'ol/layer';

export interface Overlay {
  type: string;
  olLayer: Layer;
}
