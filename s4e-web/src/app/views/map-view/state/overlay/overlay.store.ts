/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Injectable} from '@angular/core';
import {
  EntityState,
  EntityStore,
  EntityUIStore,
  MultiActiveState,
  StoreConfig
} from '@datorama/akita';
import {Overlay, OverlayUIState} from './overlay.model';

export interface OverlayState
  extends EntityState<Overlay>,
    MultiActiveState<number> {}

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Overlay'})
export class OverlayStore extends EntityStore<OverlayState, Overlay> {
  public readonly ui: EntityUIStore<OverlayUIState>;

  constructor() {
    super({
      active: []
    });
    this.createUIStore({
      showNewOverlayForm: false,
      loadingNew: false
    }).setInitialEntityState({
      loadingVisible: false,
      loadingDelete: false,
      loadingPublic: false
    });
  }
}
