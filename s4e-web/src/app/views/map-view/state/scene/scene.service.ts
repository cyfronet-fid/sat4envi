import {handleHttpRequest$} from 'src/app/common/store.util';
import {environment} from './../../../../../environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SceneStore} from './scene.store.service';
import {SceneResponse, SHOW_SCENE_DETAILS_QUERY_PARAM} from './scene.model';
import {SceneQuery} from './scene.query';
import {LegendService} from '../legend/legend.service';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {applyTransaction} from '@datorama/akita';
import {map, switchMap, tap} from 'rxjs/operators';
import {Product} from '../product/product.model';
import {timezone} from '../../../../utils/miscellaneous/date-utils';
import {ActivatedQueue} from '../../../../utils/search/activated-queue.utils';
import {Router} from '@angular/router';
import {RouterQuery} from '@datorama/akita-ng-router-store';
import {ModalService} from '../../../../modal/state/modal.service';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {makeDetailsModal, SENTINEL_SEARCH_RESULT_MODAL_ID} from '../../sentinel-search/search-result-modal/search-result-modal.model';
import {filterTrue} from '../../../../utils/rxjs/observable';
import {fromPromise} from 'rxjs/internal-compatibility';
import {createSentinelSearchResult} from '../sentinel-search/sentinel-search.model';

@Injectable({providedIn: 'root'})
export class SceneService {
  private _activatedQueue: ActivatedQueue;

  constructor(private store: SceneStore,
              private sceneQuery: SceneQuery,
              private productQuery: ProductQuery,
              private productStore: ProductStore,
              private legendService: LegendService,
              private modalQuery: ModalQuery,
              private modalService: ModalService,
              private routerQuery: RouterQuery,
              private router: Router,
              private http: HttpClient) {
    this._activatedQueue = new ActivatedQueue(this.sceneQuery, this.store, false);
  }

  get(product: Product, date: string, setActive?: 'last' | 'first') {
    const url = `${environment.apiPrefixV1}/products/${product.id}/scenes`;
    const urlParams = {params: {date, timeZone: timezone()}};

    return this.http.get<SceneResponse[]>(url, urlParams)
      .pipe(
        handleHttpRequest$(this.store),
        map(scenes => scenes.map(scene => createSentinelSearchResult(scene)).map(scene => ({...scene, layerName: product.layerName}))),
        tap(scenes => applyTransaction(() => {
          this.store.set(scenes);
          let activeSceneId = null;
          if (scenes.length > 0) {
            activeSceneId = setActive === 'last' && scenes[scenes.length - 1].id
              || setActive === 'first' && scenes[0].id
              || null;
          }
          this.store.setActive(activeSceneId);
        }))
      );
  }

  setActive(sceneId: number | null) {
    this.store.setActive(sceneId);

    const activeScene = this.sceneQuery.getActive();
    const sceneLegend = activeScene == null ? null : activeScene.legend;
    if (sceneLegend != null) {
      this.legendService.set(sceneLegend);
      return;
    }

    const activeProduct = this.productQuery.getActive();
    if (activeProduct != null) {
      this.legendService.set(activeProduct.legend);
    }
  }

  previous(): boolean {
    return this._activatedQueue.previous();
  }

  next(): boolean {
    return this._activatedQueue.next();
  }

  showModalForActive(show: boolean = true) {
    if (this.sceneQuery.getActive() == null) {
      return;
    }
    return this.router.navigate([], {
      replaceUrl: true,
      queryParams: {[SHOW_SCENE_DETAILS_QUERY_PARAM]: show ? '1' : undefined},
      queryParamsHandling: 'merge'
    });
  }

  connectQueryToDetailsModal$() {
    return this.routerQuery.selectQueryParams(SHOW_SCENE_DETAILS_QUERY_PARAM).pipe(
      map(param => param === '1'),
      filterTrue(),
      switchMap(() => {
        this.modalService.show(makeDetailsModal(false, 'scene', this.sceneQuery.getActive() as any));
        return this.modalQuery.modalClosed$(SENTINEL_SEARCH_RESULT_MODAL_ID);
      }),
      switchMap(() => fromPromise(this.showModalForActive(false)))
    );
  }
}
