import { RemoteConfiguration } from 'src/app/utils/initializer/config.service';
import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {OverlayState, OverlayStore} from './overlay.store';
import {convertToUIOverlay, Overlay, UIOverlay} from './overlay.model';
import {combineLatest, Observable} from 'rxjs';
import {filter, map, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class OverlayQuery extends QueryEntity<OverlayState, Overlay> {
  constructor(
    protected _store: OverlayStore,
    private _remoteConfiguration: RemoteConfiguration
  ) {
    super(_store);
  }

  public selectAllAsUIOverlays(): Observable<UIOverlay[]> {
    return combineLatest(this.selectAll(), this.selectActiveId()).pipe(
      map(([overlays, activeId]) => overlays
        .map(overlay => convertToUIOverlay(
          overlay,
          this._remoteConfiguration.get().geoserverUrl,
          activeId.includes(overlay.id)
        ))
      )
    );
  }

  selectActiveUIOverlays(): Observable<UIOverlay[]> {
    return this.selectAllAsUIOverlays()
      .pipe(map(layers => layers.filter(l => l.active)));
  }
}
