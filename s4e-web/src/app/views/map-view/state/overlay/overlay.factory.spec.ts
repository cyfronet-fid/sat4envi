import * as Factory from 'factory.ts';
import {Overlay} from './overlay.model';

export const OverlayFactory = Factory.makeFactory<Overlay>({
  id: Factory.each(i => `overlay:${i}`),
  caption: Factory.each(i => `Overlay #${i}`),
  type: 'wms'
});
