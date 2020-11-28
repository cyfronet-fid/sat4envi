import * as Factory from 'factory.ts';
import { Overlay, GLOBAL_OWNER_TYPE } from './overlay.model';

export const OverlayFactory = Factory.makeFactory<Overlay>({
  id: Factory.each(i => i),
  label: Factory.each(i => `Overlay #${i}`),
  ownerType: GLOBAL_OWNER_TYPE,
  visible: true,
  url: 'http://localhost:8080/geoserver/wms',
  createdAt: null
});
