import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {OverlayState, OverlayStore} from './overlay.store';
import {Overlay, OverlayUI, OverlayUIState, UIOverlay} from './overlay.model';
import {combineLatest, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import { convertToUIOverlay } from './overlay.utils';

@Injectable({
  providedIn: 'root'
})
export class OverlayQuery extends QueryEntity<OverlayState, Overlay> {
  public readonly ui: QueryEntity<OverlayUIState, OverlayUI>;

  constructor(
    protected _store: OverlayStore
  )  {
    super(_store);
    this.createUIQuery();
  }

  public selectVisibleAsUIOverlays(): Observable<UIOverlay[]> {
    return combineLatest(this.selectAll(), this.selectActiveId()).pipe(
      map(([overlays, activeId]) => overlays
        .filter(overlay => overlay.visible)
        .map(overlay => convertToUIOverlay(
          overlay,
          activeId.includes(overlay.id)
        )))
    );
  }

  public selectActiveUIOverlays(): Observable<UIOverlay[]> {
    return this.selectVisibleAsUIOverlays()
      .pipe(map(layers => layers.filter(l => l.active)));
  }

  public selectAllWithUIState(): Observable<(Overlay & OverlayUI)[]> {
    return combineLatest([
      this.selectAll(),
      this.ui.selectAll()
    ]).pipe(map(([overlays, uis]) => overlays.map(ol => ({...ol, ...this.ui.getEntity(ol.id)}))));
  }
}
