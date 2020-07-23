import * as Factory from 'factory.ts';
import {Overlay} from './overlay.model';

export const OverlayFactory = Factory.makeFactory<Overlay>({
  id: Factory.each(i => `overlay:${i}`),
  caption: Factory.each(i => `Overlay #${i}`),
  type: 'wms',
  visible: true,
  mine: false,
  layerName: Factory.each(i => `Overlay Name #${i}`),
  url: 'http://localhost:8080/geoserver/wms',
  created: null
});
