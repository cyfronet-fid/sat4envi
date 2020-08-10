import { handleHttpRequest$ } from 'src/app/common/store.util';
import { RemoteConfiguration } from 'src/app/utils/initializer/config.service';
import {Injectable} from '@angular/core';
import {OverlayStore} from './overlay.store';
import {OverlayResponse, OverlayType, Overlay} from './overlay.model';
import {map} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {action} from '@datorama/akita';
import environment from 'src/environments/environment';

/**
 * This is stub service which will be responsible for getting overlay data
 * when it's backend implementation is finished
 */
@Injectable({providedIn: 'root'})
export class OverlayService {
  constructor(
    private _store: OverlayStore,
    private _http: HttpClient,
    private _remoteConfiguration: RemoteConfiguration) {
  }

  get() {
    const url = `${environment.apiPrefixV1}/overlays/prg/`;
    const get$ = this._http.get<OverlayResponse[]>(url)
      .pipe(
        handleHttpRequest$(this._store),
        map(overlays => overlays.map(response => this._toOverlay(response)))
      )
      .subscribe((data) => this._store.set(data));
  }

  setActive(overlayId: string | null) {
    this._store.toggleActive(overlayId);
  }

  @action('setAllActive')
  setAllActive(overlayIds: string[]) {
    this._store.setActive(overlayIds);
  }

  private _toOverlay(response: OverlayResponse): Overlay {
    return {
      id: this._remoteConfiguration.get().geoserverWorkspace + ':' + response.layerName,
      caption: response.name,
      type: 'wms' as OverlayType
    };
  }

}
