import {Injectable} from '@angular/core';
import {OverlayStore} from './overlay.store';
import {OverlayType} from './overlay.model';
import {of} from 'rxjs';
import {finalize} from 'rxjs/operators';
import {S4eConfig} from '../../../../utils/initializer/config.service';

/**
 * This is stub service which will be responsible for getting overlay data
 * when it's backend implementation is finished
 */
@Injectable({providedIn: 'root'})
export class OverlayService {
  constructor(private overlayStore: OverlayStore,
              private CONFIG: S4eConfig) {
  }

  get() {
    this.overlayStore.setLoading(true);
    // :TODO replace mock with HTTP request
    of([{
      id: this.CONFIG.geoserverWorkspace + ':wojewodztwa',
      caption: 'regions',
      type: 'wms' as OverlayType
    }]).pipe(finalize(() => this.overlayStore.setLoading(false))).subscribe(overlays => this.overlayStore.set(overlays));
  }
}
