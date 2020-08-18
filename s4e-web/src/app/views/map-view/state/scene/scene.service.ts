import { handleHttpRequest$ } from 'src/app/common/store.util';
import { environment } from './../../../../../environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SceneStore} from './scene.store.service';
import {Scene} from './scene.model';
import {SceneQuery} from './scene.query.service';
import {LegendService} from '../legend/legend.service';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {applyTransaction} from '@datorama/akita';
import { map, finalize } from 'rxjs/operators';
import {Product} from '../product/product.model';

@Injectable({providedIn: 'root'})
export class SceneService {

  constructor(private store: SceneStore,
              private sceneQuery: SceneQuery,
              private productQuery: ProductQuery,
              private productStore: ProductStore,
              private legendService: LegendService,
              private http: HttpClient) {
  }

  get(product: Product, date: string) {
    const url = `${environment.apiPrefixV1}/products/${product.id}/scenes`;
    const urlParams = {params: {date, timeZone: environment.timezone}};
    const get$ = this.http.get<Scene[]>(url, urlParams)
      .pipe(
        handleHttpRequest$(this.store),
        map(scenes => scenes.map(scene => ({...scene, layerName: product.layerName})))
      )
      .subscribe((scenes) => applyTransaction(() => this.store.set(scenes)));
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
}
