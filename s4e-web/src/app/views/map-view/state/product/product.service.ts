import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product.store';
import {Product} from './product.model';
import {finalize} from 'rxjs/operators';
import {ProductTypeQuery} from '../product-type/product-type.query';
import {ProductQuery} from './product.query';
import {ProductTypeStore} from '../product-type/product-type.store';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {LegendService} from '../legend/legend.service';

@Injectable({providedIn: 'root'})
export class ProductService {

  constructor(private productStore: ProductStore,
              private productQuery: ProductQuery,
              private productTypeQuery: ProductTypeQuery,
              private productTypeStore: ProductTypeStore,
              private legendService: LegendService,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  get(productTypeId: number) {
    this.productStore.setLoading(true);
    this.productStore.set([]);
    this.http.get<Product[]>(`${this.CONFIG.apiPrefixV1}/products/productTypeId/${productTypeId}`).pipe(
      finalize(() => this.productStore.setLoading(false))
    ).subscribe((entities) => {
      this.productStore.set(entities);
      let lastProductId: number | null = null;
      if (this.productQuery.getCount() > 0) {
        lastProductId = this.productQuery.getAll()[this.productQuery.getCount() - 1].id;
      }

      this.productTypeStore.setActive(productTypeId);
      this.setActive(lastProductId);
    });
  }

  setActive(productId: number) {
    this.productStore.setActive(productId);
    const productLegend = this.productQuery.getActive().legend;
    if(productLegend != null) {
      this.legendService.set(productLegend)
    } else {
      this.legendService.set(this.productTypeQuery.getActive().legend);
    }
  }
}
