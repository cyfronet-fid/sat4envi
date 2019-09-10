import {Inject, Injectable} from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { OverlayStore, OverlayState } from './overlay.store';
import {convertToUIOverlay, Overlay, UIOverlay} from './overlay.model';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';
import {InitService} from '../../../../utils/initializer/init.service';
import {IConfiguration} from '../../../../app.configuration';
import {S4E_CONFIG} from '../../../../utils/initializer/config.service';

@Injectable({
  providedIn: 'root'
})
export class OverlayQuery extends QueryEntity<OverlayState, Overlay> {
  constructor(protected store: OverlayStore,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              @Inject(S4E_CONFIG) private CONFIG: IConfiguration
              ) {
    super(store);
  }

  public selectAllAsUIOverlays(): Observable<UIOverlay[]> {
    return this.selectAll().pipe(
      map(overlays => overlays.map(ol => convertToUIOverlay(ol, this.CONFIG.geoserverUrl, true)))
    );
  }
}
