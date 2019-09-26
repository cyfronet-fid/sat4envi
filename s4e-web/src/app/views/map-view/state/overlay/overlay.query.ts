import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {OverlayState, OverlayStore} from './overlay.store';
import {convertToUIOverlay, Overlay, UIOverlay} from './overlay.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
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
    return this.selectAll().pipe(
      map(overlays => overlays.map(ol => convertToUIOverlay(ol, this.CONFIG.geoserverUrl, true)))
    );
  }
}
