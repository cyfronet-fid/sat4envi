import { Injectable } from '@angular/core';
import {ModalService} from '../../../../modal/state/modal.service';
import {filter, map, tap} from 'rxjs/operators';
import {ProductService} from '../product/product.service';
import Timer = NodeJS.Timer;
import {SceneStore} from './scene.store.service';
import {SceneQuery} from './scene.query';
import environment from '../../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class TimelineService {
  private _updaterIntervalID: Timer;
  private _handleUpdater$ = this._sceneQuery.selectLoading()
    .pipe(
      filter(isLoading => !isLoading),
      map(() => this._sceneQuery.getValue().isLiveMode),
      tap(isLiveMode => isLiveMode ? this._turnOnUpdater() : this._turnOffUpdater())
    );

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
    if (!!this._updaterIntervalID) {
      return;
    }

    this._productService.getLastAvailableScene();
    this._updaterIntervalID = setInterval(
      () => this._productService.getLastAvailableScene(),
      environment.liveSceneUpdateRateInMs
    );
  }

  private _turnOffUpdater() {
    if (!this._updaterIntervalID) {
      return;
    }

    clearInterval(this._updaterIntervalID);
    this._updaterIntervalID = null;
  }
}
