import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product.store';
import {Product, ProductResponse} from './product.model';
import {finalize, map} from 'rxjs/operators';
import {ProductTypeQuery} from '../product-type/product-type.query';
import {deserializeJsonResponse} from '../../../../utils/miscellaneous/miscellaneous';
import {ProductQuery} from './product.query';
import {ProductTypeStore} from '../product-type/product-type.store';
import {S4eConfig} from '../../../../utils/initializer/config.service';

@Injectable({providedIn: 'root'})
export class ProductService {

  constructor(private productStore: ProductStore,
              private productQuery: ProductQuery,
              private productTypeQuery: ProductTypeQuery,
              private productTypeStore: ProductTypeStore,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  get(productTypeId: number) {
    this.productStore.setLoading(true);
    this.http.get<Product[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${productTypeId}`).pipe(
      finalize(() => this.productStore.setLoading(false)),
      map(data => deserializeJsonResponse(data, ProductResponse))
    ).subscribe((entities) => {
      this.productStore.add(entities);
      let lastProductId: number | null = null;
      if (this.productQuery.getCount() > 0) {
        lastProductId = this.productQuery.getAll()[this.productQuery.getCount() - 1].id;
      }

      this.productStore.setActive(lastProductId);
      this.productTypeStore.update(productTypeId, productType => ({...productType, productIds: entities.map(granule => granule.id)}));
      this.productTypeStore.setActive(productTypeId);
    });
  }

  setActive(productId: number) {
    this.productStore.setActive(productId);
  }
}
