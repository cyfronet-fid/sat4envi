import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SceneStore} from './scene.store.service';
import {Scene} from './scene.model';
import {finalize} from 'rxjs/operators';
import {SceneQuery} from './scene.query.service';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {LegendService} from '../legend/legend.service';
import {ProductQuery} from '../product/product.query';
import {ProductStore} from '../product/product.store';

@Injectable({providedIn: 'root'})
export class SceneService {

  constructor(private sceneStore: SceneStore,
              private sceneQuery: SceneQuery,
              private productQuery: ProductQuery,
              private productStore: ProductStore,
              private legendService: LegendService,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  get(productId: number, date: string) {
    this.sceneStore.setLoading(true);
    this.sceneStore.set([]);
    this.http.get<Scene[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${productId}`, {params: {date}}).pipe(
      finalize(() => this.sceneStore.setLoading(false))
    ).subscribe((entities) => {
      this.sceneStore.set(entities);
      let lastProductId: number | null = null;
      if (this.sceneQuery.getCount() > 0) {
        lastProductId = this.sceneQuery.getAll()[this.sceneQuery.getCount() - 1].id;
      }

      this.productStore.setActive(productId);
      this.setActive(lastProductId);
    });
  }

  setActive(productId: number|null) {
    this.sceneStore.setActive(productId);

    const productLegend = productId == null ? null : this.sceneQuery.getActive().legend;
    if(productLegend != null) {
      this.legendService.set(productLegend)
    } else {
      this.legendService.set(this.productQuery.getActive().legend);
    }
  }
}
