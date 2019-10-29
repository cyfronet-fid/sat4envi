import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {OverlayState, OverlayStore} from './overlay.store';
import {convertToUIOverlay, Overlay, UIOverlay} from './overlay.model';
import {combineLatest, Observable} from 'rxjs';
import {filter, map, tap} from 'rxjs/operators';
import {S4eConfig} from '../../../../utils/initializer/config.service';

@Injectable({
  providedIn: 'root'
})
export class OverlayQuery extends QueryEntity<OverlayState, Overlay> {
  constructor(protected store: OverlayStore,
              private CONFIG: S4eConfig
  ) {
    super(store);
  }

  public selectAllAsUIOverlays(): Observable<UIOverlay[]> {
    return combineLatest(this.selectAll(), this.selectActiveId()).pipe(
      map(([overlays, activeId]) => overlays.map(ol => convertToUIOverlay(ol, this.CONFIG.geoserverUrl, activeId.includes(ol.id))))
    );
  }

  selectActiveUIOverlays(): Observable<UIOverlay[]> {
    return this.selectAllAsUIOverlays().pipe(map(layers => layers.filter(l => l.active)));
  }
}
