import {handleHttpRequest$} from 'src/app/common/store.util';
import {RemoteConfiguration} from 'src/app/utils/initializer/config.service';
import {Injectable} from '@angular/core';
import {OverlayStore} from './overlay.store';
import {createOverlay, Overlay, OverlayResponse, OverlayType} from './overlay.model';
import {delay, finalize, map} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {action, guid} from '@datorama/akita';
import {of} from 'rxjs';
import {OverlayQuery} from './overlay.query';
import environment from 'src/environments/environment';

/**
 * This is stub service which will be responsible for getting overlay data
 * when it's backend implementation is finished
 */
@Injectable({providedIn: 'root'})
export class OverlayService {
  constructor(private _store: OverlayStore,
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
      .subscribe(overlays => this._store.set(overlays));
  }

  setActive(overlayId: string | null) {
    this._store.toggleActive(overlayId);
  }

  @action('setAllActive')
  setAllActive(overlayIds: string[]) {
    this._store.setActive(overlayIds);
  }

  setVisible(id: string, visible: boolean) {
    this._store.ui.update(id, {loadingVisible: true});
    of([true]).pipe(
      delay(500),
      finalize(() => this._store.ui.update(id, {loadingVisible: false}))
    ).subscribe(() => {
        if (!visible) {
          this._store.removeActive(id);
        }
        this._store.update(id, {visible});
        this._store.ui.update({showNewOverlayForm: false});
      },
      error => this._store.ui.setError(error)
    );
  }

  setNewFormVisible(show: boolean) {
    this._store.ui.update({showNewOverlayForm: show});
  }

  createOverlay(layerName: string, url: string, layer: string) {
    this._store.ui.update({loadingNew: true});
    of([createOverlay({url: url, layerName: layer, caption: layerName, mine: true, type: 'wms', id: guid()})])
      .pipe(
        delay(500),
        finalize(() => this._store.ui.update({loadingNew: false}))
      ).subscribe((overlay) => {
        this._store.add(overlay);
        this._store.ui.update({showNewOverlayForm: false});
      },
      error => this._store.ui.setError(error)
    );
  }

  deleteOverlay(id: string) {
    this._store.ui.update(id, {loadingDelete: true});
    of([true]).pipe(
      delay(500),
      finalize(() => this._store.ui.update(id, {loadingDelete: false}))
    ).subscribe(() => {
        this._store.remove(id);
        this._store.ui.update({showNewOverlayForm: false});
      },
      error => this._store.ui.setError(error)
    );
  }

  resetUI() {
    this._store.ui.update({showNewOverlayForm: false, loadingNew: false, error: null, loading: false});
    this._store.ui.update(null, {loadingDelete: false, loadingPublic: false, loadingVisible: false});
  }

  private _toOverlay(overlayResponse: OverlayResponse): Overlay {
    return {
      id: this._remoteConfiguration.get().geoserverWorkspace + ':' + overlayResponse.layerName,
      caption: overlayResponse.name,
      type: 'wms' as OverlayType,
      url: this._remoteConfiguration.get().geoserverUrl,
      layerName: overlayResponse.layerName,
      created: null,
      mine: false,
      visible: true
    };
  }
}
