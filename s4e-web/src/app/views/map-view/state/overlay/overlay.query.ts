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
import {QueryEntity} from '@datorama/akita';
import {OverlayState, OverlayStore} from './overlay.store';
import {Overlay, OverlayUI, OverlayUIState, UIOverlay} from './overlay.model';
import {combineLatest, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {convertToUIOverlay} from './overlay.utils';

@Injectable({
  providedIn: 'root'
})
export class OverlayQuery extends QueryEntity<OverlayState, Overlay> {
  public readonly ui: QueryEntity<OverlayUIState, OverlayUI>;

  constructor(protected _store: OverlayStore) {
    super(_store);
    this.createUIQuery();
  }

  public selectVisibleAsUIOverlays(): Observable<UIOverlay[]> {
    return combineLatest(this.selectAll(), this.selectActiveId()).pipe(
      map(([overlays, activeId]) =>
        overlays
          .filter(overlay => overlay.visible)
          .map(overlay => convertToUIOverlay(overlay, activeId.includes(overlay.id)))
      )
    );
  }

  public selectActiveUIOverlays(): Observable<UIOverlay[]> {
    return this.selectVisibleAsUIOverlays().pipe(
      map(layers => layers.filter(l => l.active))
    );
  }

  public selectAllWithUIState(): Observable<(Overlay & OverlayUI)[]> {
    return combineLatest([this.selectAll(), this.ui.selectAll()]).pipe(
      map(([overlays, uis]) =>
        overlays.map(ol => ({...ol, ...this.ui.getEntity(ol.id)}))
      )
    );
  }
}
