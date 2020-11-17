import { handleHttpRequest$ } from 'src/app/common/store.util';
import { environment } from './../../../../../environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SceneStore} from './scene.store.service';
import {Scene} from './scene.model';
import {SceneQuery} from './scene.query';
import {LegendService} from '../legend/legend.service';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {applyTransaction} from '@datorama/akita';
import {map, finalize, tap} from 'rxjs/operators';
import {Product} from '../product/product.model';
import {timezone} from '../../../../utils/miscellaneous/date-utils';
import {ActivatedQueue} from '../../../../utils/search/activated-queue.utils';
import * as moment from 'moment';

@Injectable({providedIn: 'root'})
export class SceneService {
  private _activatedQueue: ActivatedQueue;

  constructor(private store: SceneStore,
              private sceneQuery: SceneQuery,
              private productQuery: ProductQuery,
              private productStore: ProductStore,
              private legendService: LegendService,
              private http: HttpClient) {
    this._activatedQueue = new ActivatedQueue(this.sceneQuery, this.store, false);
  }

  get(product: Product, date: string, setActive?: 'last'|'first') {
    const url = `${environment.apiPrefixV1}/products/${product.id}/scenes`;
    const urlParams = {params: {
      date: moment(date).format('YYYY-MM-DD'),
      timeZone: timezone()
    }};

    return this.http.get<Scene[]>(url, urlParams)
      .pipe(
        handleHttpRequest$(this.store),
        map(scenes => scenes.map(scene => ({...scene, layerName: product.layerName}))),
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
      )
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
}
