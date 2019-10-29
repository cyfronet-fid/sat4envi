import { Injectable } from '@angular/core';
import {EntityState, EntityStore, MultiActiveState, StoreConfig} from '@datorama/akita';
import { Overlay } from './overlay.model';

export interface OverlayState extends EntityState<Overlay>, MultiActiveState<string> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Overlay' })
export class OverlayStore extends EntityStore<OverlayState, Overlay> {

  constructor() {
    super({
      active: []
    });
  }

}

