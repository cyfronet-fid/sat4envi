import {Injectable} from '@angular/core';
import {OverlayStore} from './overlay.store';
import {OverlayResponse, OverlayType} from './overlay.model';
import {map} from 'rxjs/operators';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {OverlayQuery} from './overlay.query';
import {HttpClient} from '@angular/common/http';
import {action} from '@datorama/akita';
import {catchErrorAndHandleStore} from '../../../../common/store.util';

/**
 * This is stub service which will be responsible for getting overlay data
 * when it's backend implementation is finished
 */
@Injectable({providedIn: 'root'})
export class OverlayService {
  constructor(private store: OverlayStore,
              private overlayQuery: OverlayQuery,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  get() {
    this.store.setLoading(true);
    // :TODO replace mock with HTTP request

    this.http.get<OverlayResponse[]>(`${this.CONFIG.apiPrefixV1}/overlays/prg/`)
      .pipe(
        catchErrorAndHandleStore(this.store),
        map(or => or.map(o => ({
          id: this.CONFIG.geoserverWorkspace + ':' + o.layerName,
          caption: o.name,
          type: 'wms' as OverlayType
        }))))
      .subscribe(
        overlays => this.store.set(overlays)
      );
  }

  setActive(overlayId: string | null) {
    this.store.toggleActive(overlayId);
  }

  @action('setAllActive')
  setAllActive(overlayIds: string[]) {
    this.store.setActive(overlayIds);
  }

}
