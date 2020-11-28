import {Injectable} from '@angular/core';
import {EntityState, EntityStore, EntityUIStore, MultiActiveState, StoreConfig} from '@datorama/akita';
import {Overlay, OverlayUIState} from './overlay.model';

export interface OverlayState extends EntityState<Overlay>, MultiActiveState<number> {
}

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Overlay'})
export class OverlayStore extends EntityStore<OverlayState, Overlay> {
  public readonly ui: EntityUIStore<OverlayUIState>;

  constructor() {
    super({
      active: []
    });
    this.createUIStore({showNewOverlayForm: false, loadingNew: false})
      .setInitialEntityState({LoadingVisible: false, LoadingDelete: false, LoadingPublic: false});
  }

}

