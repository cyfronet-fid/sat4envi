/*
 * Copyright 2021 ACC Cyfronet AGH
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

import { Injectable } from '@angular/core';
import {ModalService} from '../../../../modal/state/modal.service';
import {filter, map, switchMap, tap} from 'rxjs/operators';
import {ProductService} from '../product/product.service';
import Timer = NodeJS.Timer;
import {SceneStore} from './scene.store.service';
import {SceneQuery} from './scene.query';
import environment from '../../../../../environments/environment';
import {interval, Subscription} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TimelineService {
  private _handleUpdater$ = this._sceneQuery.selectLoading()
    .pipe(
      filter(isLoading => !isLoading),
      map(() => this._sceneQuery.getValue().isLiveMode),
      tap(isLiveMode => isLiveMode ? this._turnOnUpdater() : this._turnOffUpdater())
    );

  private _intervalSubscription: Subscription;

  constructor(
    private _sceneStore: SceneStore,
    private _sceneQuery: SceneQuery,
    private _modalService: ModalService,
    private _productService: ProductService
  ) {
    this._handleUpdater$.subscribe();
  }

  toggleLiveMode(isLiveMode?: boolean) {
    this._sceneStore.setLoading(true);
    isLiveMode = isLiveMode != null
      ? isLiveMode
      : !this._sceneQuery.getValue().isLiveMode;

    this._sceneStore.update({...this._sceneQuery.getValue(), isLiveMode});
    this._sceneStore.setLoading(false);
  }

  async confirmTurningOfLiveMode(): Promise<boolean> {
    this._sceneStore.setLoading(true);

    const isLiveMode = this._sceneQuery.getValue().isLiveMode;
    if (!isLiveMode) {
      this._sceneStore.setLoading(false);
      return true;
    }

    const isLeavingLiveMode = await this._modalService.confirm(
      'Wychodzisz z trybu podglądu scen na żywo',
      'Czy na pewno chcesz wyjść?'
    );
    if (isLeavingLiveMode) {
        this._sceneStore.update({isLiveMode: false});
    }

    this._sceneStore.setLoading(false);
    return isLeavingLiveMode;
  }

  private _turnOnUpdater() {
    if (!!this._intervalSubscription) {
      return;
    }

    this._intervalSubscription = this._productService.getLastAvailableScene$()
      .pipe(
        switchMap(() => interval(environment.liveSceneUpdateRateInMs)),
        switchMap(() => this._productService.getLastAvailableScene$())
      )
    .subscribe();
  }

  private _turnOffUpdater() {
    if (!this._intervalSubscription) {
      return;
    }

    this._intervalSubscription.unsubscribe();
    this._intervalSubscription = null;
  }
}
