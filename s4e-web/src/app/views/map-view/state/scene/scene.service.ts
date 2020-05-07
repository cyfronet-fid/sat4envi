import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SceneStore} from './scene.store.service';
import {Scene} from './scene.model';
import {SceneQuery} from './scene.query.service';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {LegendService} from '../legend/legend.service';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';
import {applyTransaction} from '@datorama/akita';
import {map} from 'rxjs/operators';
import {Product} from '../product/product.model';
import { catchErrorAndHandleStore } from 'src/app/common/store.util';

@Injectable({providedIn: 'root'})
export class SceneService {

  constructor(private store: SceneStore,
              private sceneQuery: SceneQuery,
              private productQuery: ProductQuery,
              private productStore: ProductStore,
              private legendService: LegendService,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  get(product: Product, date: string) {
    this.store.setLoading(true);
    this.http.get<Scene[]>(`${this.CONFIG.apiPrefixV1}/products/${product.id}/scenes`, {
      params: {date: date, tz: this.CONFIG.timezone}
    }).pipe(
        catchErrorAndHandleStore(this.store),
        map(entities => entities.map(s => ({...s, layerName: product.layerName})))
      )
      .subscribe((entities) => applyTransaction(() => {
        this.store.set(entities);
        if (this.sceneQuery.getCount() > 0) {
          this.sceneQuery.getAll()[this.sceneQuery.getCount() - 1].id;
        }
      }));
  }

  setActive(sceneId: number | null) {
    this.store.setActive(sceneId);
    const activeScene = this.sceneQuery.getActive();
    const sceneLegend = activeScene == null ? null : activeScene.legend;
    const activeProduct = this.productQuery.getActive();

    if (sceneLegend != null) {
      this.legendService.set(sceneLegend);
    } else if (activeProduct != null) {
      this.legendService.set(activeProduct.legend);
    }
  }
}
