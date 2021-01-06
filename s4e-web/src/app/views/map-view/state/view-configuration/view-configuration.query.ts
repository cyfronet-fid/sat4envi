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
import {ViewConfigurationState, ViewConfigurationStore} from './view-configuration.store';
import {ViewConfiguration, ViewConfigurationEx, ViewRouterConfig} from './view-configuration.model';
import {SceneQuery} from '../scene/scene.query';
import {OverlayQuery} from '../overlay/overlay.query';
import {ProductQuery} from '../product/product.query';
import {MapQuery} from '../map/map.query';
import {map} from 'rxjs/operators';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ViewConfigurationQuery extends QueryEntity<ViewConfigurationState, ViewConfiguration> {

  constructor(protected store: ViewConfigurationStore,
              private sceneQuery: SceneQuery,
              private overlayQuery: OverlayQuery,
              private productQuery: ProductQuery,
              private mapQuery: MapQuery) {
    super(store);
  }

  getCurrent(): ViewConfigurationEx {
    return this.mapToExtended({
      caption: '',
      thumbnail: null,
      configuration: {
        sceneId: this.sceneQuery.getActiveId(),
        productId: this.productQuery.getActiveId(),
        overlays: this.overlayQuery.getActiveId(),
        date: this.productQuery.getValue().ui.selectedDate,
        viewPosition: this.mapQuery.getValue().view
      } as ViewRouterConfig
    });
  }

  mapToExtended(viewConfiguration: ViewConfiguration): ViewConfigurationEx {
    return {
      ...viewConfiguration,
      configurationNames: {
        product: viewConfiguration.configuration.productId == null
          ? null
          : this.productQuery.getEntity(viewConfiguration.configuration.productId).name,
        selectedDate: viewConfiguration.configuration.date,
        overlays: viewConfiguration.configuration.overlays.map(id => this.overlayQuery.getEntity(id).label)
      }
    };
  }

  selectAllAsEx(): Observable<ViewConfigurationEx[]> {
    return this.selectAll().pipe(map(configs => configs.map(c => this.mapToExtended(c))));
  }
}
