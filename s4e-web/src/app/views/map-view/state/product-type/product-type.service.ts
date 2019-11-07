import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductTypeStore} from './product-type.store';
import {ProductType} from './product-type.model';
import {finalize, tap} from 'rxjs/operators';
import {ProductService} from '../product/product.service';
import {ProductTypeQuery} from './product-type.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {ProductStore} from '../product/product.store';
import {LegendService} from '../legend/legend.service';
import {Observable, of} from 'rxjs';

@Injectable({providedIn: 'root'})
export class ProductTypeService {

  constructor(private productTypeStore: ProductTypeStore,
              private productStore: ProductStore,
              private CONFIG: S4eConfig,
              private http: HttpClient,
              private legendService: LegendService,
              private productTypeQuery: ProductTypeQuery,
              private productService: ProductService) {
  }

  get() {
    this.productStore.setLoading(true);
    this.http.get<ProductType[]>(`${this.CONFIG.apiPrefixV1}/productTypes`).pipe(
      finalize(() => this.productStore.setLoading(false)),
    ).subscribe(data => this.productTypeStore.set(data));
  }

  setActive(productTypeId: number | null) {
    if (productTypeId != null && this.productTypeQuery.getActiveId() !== productTypeId) {
      const productType = this.productTypeQuery.getEntity(productTypeId);
      let precondition: Observable<any>|null = null;
      this.productTypeStore.setActive(productTypeId);
      this.getSingle$(productType)
        .subscribe(() => this.productService.get(productType.id));
    } else {
      this.productTypeStore.setActive(null);
      this.productStore.setActive(null);
      this.legendService.set(null);
    }
  }

  private getSingle$(productType: ProductType): Observable<any> {
    if (productType.legend === undefined) {
      return this.http.get<ProductType>(`${this.CONFIG.apiPrefixV1}/productTypes/${productType.id}`)
        .pipe(tap(pt => this.productTypeStore.upsert(productType.id, pt)));
    } else {
      return of(null);
    }
  }
}
