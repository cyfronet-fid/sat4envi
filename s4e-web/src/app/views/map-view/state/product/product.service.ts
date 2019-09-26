import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product-store.service';
import {Product, ProductResponse} from './product.model';
import {finalize, map} from 'rxjs/operators';
import {ProductTypeQuery} from '../product-type/product-type.query';
import {deserializeJsonResponse} from '../../../../utils/miscellaneous/miscellaneous';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {ProductQuery} from './product-query.service';
import {ProductTypeStore} from '../product-type/product-type.store';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {RecentViewStore} from '../recent-view/recent-view.store';

@Injectable({providedIn: 'root'})
export class ProductService {

  constructor(private productStore: ProductStore,
              private productQuery: ProductQuery,
              private productTypeQuery: ProductTypeQuery,
              private productTypeStore: ProductTypeStore,
              private recentViewQuery: RecentViewQuery,
              private recentViewStore: RecentViewStore,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  get(productId: number) {
    this.productStore.setLoading(true);
    this.http.get<Product[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${productId}`).pipe(
      finalize(() => this.productStore.setLoading(false)),
      map(data => deserializeJsonResponse(data, ProductResponse))
    ).subscribe((entities) => {
      this.productStore.add(entities);
      const activeView = this.recentViewQuery.getActive();
      let lastProductTypeId: number | null = null;

      if (activeView != null && activeView.productId) {
        lastProductTypeId = activeView.productId;
      } else if (this.productQuery.getCount() > 0) {
        lastProductTypeId = this.productQuery.getAll()[this.productQuery.getCount() - 1].id;
      }

      this.productStore.setActive(lastProductTypeId);
      this.recentViewStore.updateActive(active => ({...active, productId: lastProductTypeId}));
      this.productTypeStore.update(productId, productType => ({...productType, productIds: entities.map(granule => granule.id)}));
    });
  }
}
