import {handleHttpRequest$} from 'src/app/common/store.util';
import {RemoteConfiguration} from 'src/app/utils/initializer/config.service';
import {Injectable} from '@angular/core';
import {OverlayStore} from './overlay.store';
import {createOverlay, Overlay, OverlayResponse, OverlayType} from './overlay.model';
import {delay, finalize, map} from 'rxjs/operators';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {OverlayResponse, OverlayType, Overlay} from './overlay.model';
import {map} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {action, guid} from '@datorama/akita';
import {catchErrorAndHandleStore} from '../../../../common/store.util';
import {of} from 'rxjs';
import {OverlayQuery} from './overlay.query';
import {action} from '@datorama/akita';
import environment from 'src/environments/environment';

/**
 * This is stub service which will be responsible for getting overlay data
 * when it's backend implementation is finished
 */
@Injectable({providedIn: 'root'})
export class OverlayService {
  constructor(private store: OverlayStore,
              private http: HttpClient,
              private overlayQuery: OverlayQuery,
              private _remoteConfiguration: RemoteConfiguration) {
  }

  get() {
    const url = `${environment.apiPrefixV1}/overlays/prg/`;
    const get$ = this._http.get<OverlayResponse[]>(url)
      .pipe(
        handleHttpRequest$(this._store),
        map(overlays => overlays.map(response => this._toOverlay(response)))
      )
      .subscribe(overlays => this._store.set(overlays));
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
      type: 'wms' as OverlayType,
      url: this.CONFIG.geoserverUrl,
      layerName: o.layerName,
      created: null,
      mine: false,
      visible: true
    };
  }

  setVisible(id: string, visible: boolean) {
    this.store.ui.update(id, {loadingVisible: true});
    of([true]).pipe(
      delay(500),
      finalize(() => this.store.ui.update(id, {loadingVisible: false}))
    ).subscribe(() => {
        if (!visible) {
          this.store.removeActive(id);
        }
        this.store.update(id, {visible});
        this.store.ui.update({showNewOverlayForm: false});
      },
      error => this.store.ui.setError(error)
    );
  }

  setNewFormVisible(show: boolean) {
    this.store.ui.update({showNewOverlayForm: show});
  }

  createOverlay(layerName: string, url: string, layer: string) {
    this.store.ui.update({loadingNew: true});
    of([createOverlay({url: url, layerName: layer, caption: layerName, mine: true, type: 'wms', id: guid()})])
      .pipe(
        delay(500),
        finalize(() => this.store.ui.update({loadingNew: false}))
      ).subscribe((overlay) => {
        this.store.add(overlay);
        this.store.ui.update({showNewOverlayForm: false});
      },
      error => this.store.ui.setError(error)
    );
  }

  deleteOverlay(id: string) {
    this.store.ui.update(id, {loadingDelete: true});
    of([true]).pipe(
      delay(500),
      finalize(() => this.store.ui.update(id, {loadingDelete: false}))
    ).subscribe(() => {
        this.store.remove(id);
        this.store.ui.update({showNewOverlayForm: false});
      },
      error => this.store.ui.setError(error)
    );
  }

  resetUI() {
    this.store.ui.update({showNewOverlayForm: false, loadingNew: false, error: null, loading: false});
    this.store.ui.update(null, {loadingDelete: false, loadingPublic: false, loadingVisible: false});
  }
}
