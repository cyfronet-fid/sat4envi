import {Inject, Injectable} from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { OverlayStore, OverlayState } from './overlay.store';
import {convertToUIOverlay, Overlay, UIOverlay} from './overlay.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';

@Injectable({
  providedIn: 'root'
})
export class OverlayQuery extends QueryEntity<OverlayState, Overlay> {
  constructor(protected store: OverlayStore, @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) {
    super(store);
  }

  public selectAllAsUIOverlays(): Observable<UIOverlay[]> {
    return this.selectAll().pipe(
      map(overlays => overlays.map(ol => convertToUIOverlay(ol, this.CONSTANTS.geoserverUrl, true)))
    );
  }
}
