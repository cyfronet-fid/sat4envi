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

import {handleHttpRequest$} from 'src/app/common/store.util';
import {environment} from '../../../../../environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SceneStore} from './scene.store.service';
import {Scene, SceneResponse, SHOW_SCENE_DETAILS_QUERY_PARAM} from './scene.model';
import {SceneQuery} from './scene.query';
import {LegendService} from '../legend/legend.service';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {applyTransaction} from '@datorama/akita';
import {map, switchMap, tap} from 'rxjs/operators';
import {Product} from '../product/product.model';
import {timezone} from '../../../../utils/miscellaneous/date-utils';
import {ActivatedQueue} from '../../../../utils/search/activated-queue.utils';
import {Router} from '@angular/router';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {ModalService} from '../../../../modal/state/modal.service';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {
  makeDetailsModal,
  SENTINEL_SEARCH_RESULT_MODAL_ID
} from '../../sentinel-search/search-result-modal/search-result-modal.model';
import {filterTrue} from '../../../../utils/rxjs/observable';
import {fromPromise} from 'rxjs/internal-compatibility';
import {createSentinelSearchResult} from '../sentinel-search/sentinel-search.model';
import {Observable} from 'rxjs';

@Injectable({providedIn: 'root'})
export class SceneService {
  private _activatedQueue: ActivatedQueue;

  constructor(
    private store: SceneStore,
    private sceneQuery: SceneQuery,
    private productQuery: ProductQuery,
    private productStore: ProductStore,
    private legendService: LegendService,
    private modalQuery: ModalQuery,
    private modalService: ModalService,
    private routerQuery: RouterQuery,
    private router: Router,
    private http: HttpClient
  ) {
    this._activatedQueue = new ActivatedQueue(this.sceneQuery, this.store, false);
  }

  get(
    product: Product,
    date: string,
    setActive?: 'last' | 'first'
  ): Observable<SceneResponse[]> {
    const url = `${environment.apiPrefixV1}/products/${product.id}/scenes`;
    const urlParams = {params: {date, timeZone: timezone()}};

    return this.http.get<SceneResponse[]>(url, urlParams).pipe(
      handleHttpRequest$(this.store),
      map(scenes =>
        scenes
          .map(scene => createSentinelSearchResult(scene))
          .map(scene => ({...scene, layerName: product.layerName}))
      ),
      tap(scenes =>
        applyTransaction(() => {
          this.store.set(scenes);
          let activeScene: Scene = null;
          if (scenes.length > 0) {
            activeScene =
              (setActive === 'last' && scenes[scenes.length - 1]) ||
              (setActive === 'first' && scenes[0]) ||
              null;
          }
          if (activeScene) {
            this.store.setActive(activeScene.id);
            this.productStore.setSelectedDate(activeScene.timestamp, true);
          } else {
            this.store.setActive(null);
          }
        })
      )
    );
  }

  setActive(sceneId: number | null, manualTrigger: boolean = false) {
    this.store.setActive(sceneId);
    if (manualTrigger) {
      this.productStore.update(state => ({
        ...state,
        ui: {
          ...state.ui,
          manuallySelectedDate: this.sceneQuery.getEntity(sceneId).timestamp
        }
      }));
    }
  }

  previous(): boolean {
    return this._activatedQueue.previous();
  }

  next(): boolean {
    return this._activatedQueue.next();
  }

  showModalForActive(show: boolean = true) {
    if (this.sceneQuery.getActive() == null) {
      return;
    }
    return this.router.navigate([], {
      replaceUrl: true,
      queryParams: {[SHOW_SCENE_DETAILS_QUERY_PARAM]: show ? '1' : undefined},
      queryParamsHandling: 'merge'
    });
  }

  connectQueryToDetailsModal$() {
    return this.routerQuery.selectQueryParams(SHOW_SCENE_DETAILS_QUERY_PARAM).pipe(
      map(param => param === '1'),
      filterTrue(),
      switchMap(() => {
        this.modalService.show(
          makeDetailsModal(false, 'scene', this.sceneQuery.getActive() as any)
        );
        return this.modalQuery.modalClosed$(SENTINEL_SEARCH_RESULT_MODAL_ID);
      }),
      switchMap(() => fromPromise(this.showModalForActive(false)))
    );
  }
}
