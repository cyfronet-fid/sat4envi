/*
 * Copyright 2020 ACC Cyfronet AGH
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
import {Query} from '@datorama/akita';
import {MapStore} from './map.store';
import {MapState} from './map.model';
import {combineLatest} from 'rxjs';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';
import {OverlayQuery} from '../overlay/overlay.query';
import {ProductQuery} from '../product/product.query';
import {SceneQuery} from '../scene/scene.query';
import {mapAllTrue} from '../../../../utils/rxjs/observable';

@Injectable({providedIn: 'root'})
export class MapQuery extends Query<MapState> {
  constructor(protected store: MapStore,
              private overlayQuery: OverlayQuery,
              private productQuery: ProductQuery,
              private sceneQuery: SceneQuery) {
    super(store);
  }

  selectQueryParamsFromStore() {
    return combineLatest([
      this.overlayQuery.selectActiveUIOverlays().pipe(map(overlays => overlays.map(o => o.id)), distinctUntilChanged()),
      this.productQuery.select().pipe(distinctUntilChanged()),
      this.sceneQuery.selectActive().pipe(distinctUntilChanged()),
      this.select().pipe(map(mv => mv.view), distinctUntilChanged())
    ]).pipe(
      map(([overlays, product, scene, zoom]) => ({
        overlays: overlays.length === 0 ? null : overlays,
        product: product == null ? null : product.active,
        scene: scene == null ? null : scene.id,
        date: product.ui.selectedDate,
        manualDate: product.ui.manuallySelectedDate || undefined,
        zoom: zoom.zoomLevel,
        centerx: zoom.centerCoordinates[0],
        centery: zoom.centerCoordinates[1]
      }))
    );
  }

  selectShowProductDescription() {
    return combineLatest([
      this.select('productDescriptionOpened'),
      this.productQuery.selectActiveId().pipe(map(id => id != null))
    ]).pipe(mapAllTrue());
  }
}
