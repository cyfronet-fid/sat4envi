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
