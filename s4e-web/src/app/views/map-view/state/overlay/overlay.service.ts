/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {handleHttpRequest$} from 'src/app/common/store.util';
import {RemoteConfiguration} from 'src/app/utils/initializer/config.service';
import {Injectable} from '@angular/core';
import {OverlayStore} from './overlay.store';
import {Overlay} from './overlay.model';
import {catchError, finalize, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {action} from '@datorama/akita';
import environment from 'src/environments/environment';
import {OverlayForm} from '../../view-manager/overlay-list-modal/overlay-list-modal.component';
import {throwError} from 'rxjs';

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
    return this._http.get<Overlay[]>(`${environment.apiPrefixV1}/overlays`)
      .pipe(
        handleHttpRequest$(this._store),
        tap(overlays => this._store.set(overlays))
      );
  }

  setActive(overlayId: number | null) {
    this._store.toggleActive(overlayId);
  }

  @action('setAllActive')
  setAllActive(overlayIds: number[]) {
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

  createGlobalOverlay$(overlay: OverlayForm) {
    const url = `${environment.apiPrefixV1}/overlays/global`;
    return this._createOverlay$(overlay, url);
  }

  createInstitutionalOverlay$(overlay: OverlayForm, institutionSlug: string) {
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/overlays`;
    return this._createOverlay$(overlay, url);
  }

  createPersonalOverlay$(overlay: OverlayForm) {
    const url = `${environment.apiPrefixV1}/overlays/personal`;
    return this._createOverlay$(overlay, url);
  }

  deletePersonalOverlay(id: number) {
    const url = `${environment.apiPrefixV1}/overlays/personal`;
    this._deleteOverlay(id, url);
  }

  deleteGlobalOverlay(id: number) {
    const url = `${environment.apiPrefixV1}/overlays/global`;
    this._deleteOverlay(id, url);
  }

  deleteInstitutionalOverlay(id: number, institutionSlug: string) {
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/overlays`;
    this._deleteOverlay(id, url);
  }

  resetUI() {
    this._store.ui.update({showNewOverlayForm: false, loadingNew: false, error: null, loading: false});
    this._store.ui.update(null, {loadingDelete: false, loadingPublic: false, loadingVisible: false});
  }

  private _createOverlay$(overlay: OverlayForm, url: string) {
    this._store.ui.update({loadingNew: true});
    return this._http.post<Overlay>(url, overlay)
      .pipe(
        tap(newOverlay => {
          this._store.add(newOverlay);
          this._store.ui.update({showNewOverlayForm: false});
        }),
        tap(() => this._store.ui.update({loadingNew: false}))
      );
  }

  private _deleteOverlay(id: number, baseUrl: string) {
    this._store.ui.update(id, {loadingDelete: true});
    this._http.delete(`${baseUrl}/${id}`)
      .pipe(finalize(() => this._store.ui.update(id, {loadingDelete: false})))
      .subscribe(() => {
          this._store.remove(id);
          this._store.ui.update({showNewOverlayForm: false});
        },
        error => this._store.ui.setError(error)
      );
  }
}
