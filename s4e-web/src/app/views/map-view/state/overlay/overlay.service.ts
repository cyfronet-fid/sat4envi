import {Inject, Injectable} from '@angular/core';
import { OverlayStore } from './overlay.store';
import {OverlayType} from './overlay.model';
import {of} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';
import {InitService} from '../../../../utils/initializer/init.service';
import {IConfiguration} from '../../../../app.configuration';
import {S4E_CONFIG} from '../../../../utils/initializer/config.service';

/**
 * This is stub service which will be responsible for getting overlay data
 * when it's backend implementation is finished
 */
@Injectable({ providedIn: 'root' })
export class OverlayService {
  constructor(private overlayStore: OverlayStore,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              @Inject(S4E_CONFIG) private CONFIG: IConfiguration) {}

  get() {
    this.overlayStore.setLoading(true);
    // :TODO replace mock with HTTP request
    of([{
      id: this.CONFIG.geoserverWorkspace + ':wojew%C3%B3dztwa',
      caption: 'regions',
      type: 'wms' as OverlayType
    }]).pipe(finalize(() => this.overlayStore.setLoading(false))).subscribe(overlays => this.overlayStore.set(overlays));
  }
}
