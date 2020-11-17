import {Injectable} from '@angular/core';
import {Query} from '@datorama/akita';
import {MapStore} from './map.store';
import {MapState} from './map.model';
import {combineLatest} from 'rxjs';
import {distinctUntilChanged, map} from 'rxjs/operators';
import {OverlayQuery} from '../overlay/overlay.query';
import {ProductQuery} from '../product/product.query';
import {SceneQuery} from '../scene/scene.query';
import * as moment from 'moment';

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
        date: !!scene && moment(scene.timestamp).format('YYYY-MM-DD[T]HH:mm:ss[Z]')
          || moment(product.ui.selectedDate).format('YYYY-MM-DD'),
        zoom: zoom.zoomLevel,
        centerx: zoom.centerCoordinates[0],
        centery: zoom.centerCoordinates[1]
      }))
    );
  }
}
