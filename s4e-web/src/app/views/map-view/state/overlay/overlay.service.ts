import {handleHttpRequest$} from 'src/app/common/store.util';
import {RemoteConfiguration} from 'src/app/utils/initializer/config.service';
import {Injectable} from '@angular/core';
import {OverlayStore} from './overlay.store';
import {Overlay} from './overlay.model';
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
    const url = `${environment.apiPrefixV1}/overlays`;
    const get$ = this._http.get<Overlay[]>(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe(overlays => this._store.set(overlays));
  }

  setActive(overlayId: string | null) {
    this._store.toggleActive(overlayId);
  }

  @action('setAllActive')
  setAllActive(overlayIds: string[]) {
    this._store.setActive(overlayIds);
  }

  setVisible(id: any, visible: boolean) {
    this._store.ui.update(id, {loadingVisible: true});

    const url = `${environment.apiPrefixV1}/overlays/${id}/visible`;
    (
      visible
        ? this._http.put(url, {})
        : this._http.delete(url)
    )
      .pipe(finalize(() => this._store.ui.update(id, {loadingVisible: false})))
      .subscribe(() => {
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

  createOverlay(label: string, url: string) {
    this._store.ui.update({loadingNew: true});
    const postUrl = `${environment.apiPrefixV1}/overlays/personal`;
    this._http.post<Overlay>(postUrl, {label, url})
      .pipe(finalize(() => this._store.ui.update({loadingNew: false})))
      .subscribe((overlay) => {
        this._store.add(overlay);
        this._store.ui.update({showNewOverlayForm: false});
      },
      error => this._store.ui.setError(error)
    );
  }

  deleteOverlay(id: string) {
    this._store.ui.update(id, {loadingDelete: true});
    const url = `${environment.apiPrefixV1}/overlays/personal/${id}`;
    this._http.delete(url)
      .pipe(finalize(() => this._store.ui.update(id, {loadingDelete: false})))
      .subscribe(() => {
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
}
