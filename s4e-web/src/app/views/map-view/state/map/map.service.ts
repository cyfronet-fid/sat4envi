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

import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MapStore} from './map.store';
import {MapQuery} from './map.query';
import {PRODUCT_DESCRIPTION_CLOSED_LOCAL_STORAGE_KEY, SIDEBAR_OPEN_LOCAL_STORAGE_KEY, ViewPosition} from './map.model';
import {of} from 'rxjs';
import {catchError, map, switchMap, take, tap} from 'rxjs/operators';
import {OverlayQuery} from '../overlay/overlay.query';
import {SceneService} from '../scene/scene.service';
import {ProductQuery} from '../product/product.query';
import {SceneQuery} from '../scene/scene.query';
import {Router} from '@angular/router';
import {OverlayService} from '../overlay/overlay.service';
import {ProductService} from '../product/product.service';
import {ViewRouterConfig} from '../view-configuration/view-configuration.model';
import {LocalStorage} from '../../../../app.providers';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {HashMap} from '@datorama/akita';
import {logIt} from '../../../../utils/rxjs/observable';
import {olx} from 'openlayers';
import view = olx.view;

@Injectable({providedIn: 'root'})
export class MapService {

  constructor(
    private store: MapStore,
    private mapQuery: MapQuery,
    private http: HttpClient,
    private overlayService: OverlayService,
    private productQuery: ProductQuery,
    private productService: ProductService,
    private sceneQuery: SceneQuery,
    private sceneService: SceneService,
    private router: Router,
    private routerQuery: RouterQuery,
    @Inject(LocalStorage) private storage: Storage
  ) {
  }

  toggleZKOptions(open: boolean = true) {
    this.store.update({zkOptionsOpened: open});
  }

  toggleLoginOptions(open: boolean = true) {
    this.store.update({loginOptionsOpened: open});
  }

  toggleProductDescription(open: boolean = true) {
    if (open === false) {
      this.storage.setItem(PRODUCT_DESCRIPTION_CLOSED_LOCAL_STORAGE_KEY, JSON.stringify(true))
    }
    this.store.update({productDescriptionOpened: open});
  }

  setView(view: ViewPosition): void {
    this.store.update({view});
  }

  public loadMapQueryParams() {
    return this.routerQuery.selectQueryParams()
      .pipe(
        map((params: HashMap<string>) => {
          const overlays: number[] = (Array.isArray(params['overlays']) ? params['overlays'] as any : [params['overlays'] as string]).map(ol => Number(ol)).filter(olId => !isNaN(olId));
          const productId = params['product'];
          const date = params['date'];
          const manualDate = params['manualDate'] || null;
          const sceneId = params['scene'];
          const centerX = params['centerx'];
          const centerY = params['centery'];
          const zoom = params['zoom'];

          // validate data, if there is something missing throw error, which will prevent setting store from those query params
          if (zoom == null || centerX == null || centerY == null || (date != null && !date.match(/^[0-9]+-[0-1][0-9]-[0-9][0-9]$/g))) {
            throw new Error('queryParams do not have all required params');
          }

          const viewPosition = {
            centerCoordinates: [Number(centerX), Number(centerY)],
            zoomLevel: Number(zoom)
          };

          return {
            overlays,
            viewPosition,
            productId: productId == null ? null : Number(productId),
            sceneId: sceneId == null ? null : Number(sceneId),
            manualDate,
            date,
          } as ViewRouterConfig;
        }),
        take(1),
        switchMap(viewConfig => this.updateStoreByView(viewConfig)),
        map(() => true),
        catchError((err) => of(false))
      );
  }

  public updateStoreByView(viewConfig: ViewRouterConfig) {
    this.productService.setSelectedDate(viewConfig.date);
    this.productService.setManualDate(viewConfig.manualDate);
    this.setView(viewConfig.viewPosition);
    this.overlayService.setAllActive(viewConfig.overlays);
    return this.productService.setActive$(viewConfig.productId).pipe(tap(() => this.sceneService.setActive(viewConfig.sceneId)));
  }

  public connectStoreToRouter() {
    return this.mapQuery.selectQueryParamsFromStore()
      .pipe(
        switchMap((queryParams) => this.router.navigate(
          [],
          {
            replaceUrl: true,
            queryParamsHandling: 'merge',
            queryParams: queryParams
          }
        ))
      );
  }

  showSidebar(show: boolean) {
    this.store.update(state => ({...state, sidebarOpen: show}));
    this.storage.setItem(SIDEBAR_OPEN_LOCAL_STORAGE_KEY, JSON.stringify(show));
  }

  toggleSidebar() {
    this.showSidebar(!this.mapQuery.getValue().sidebarOpen);
  }
}

