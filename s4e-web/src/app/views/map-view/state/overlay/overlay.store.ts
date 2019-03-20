import { Injectable } from '@angular/core';
import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Overlay } from './overlay.model';

export interface OverlayState extends EntityState<Overlay> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Overlay' })
export class OverlayStore extends EntityStore<OverlayState, Overlay> {

  constructor() {
    super();
  }

}

