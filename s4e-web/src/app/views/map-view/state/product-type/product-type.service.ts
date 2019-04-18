import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductTypeStore} from './product-type-store.service';
import {ProductType} from './product-type.model';
import {finalize} from 'rxjs/operators';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';
import {ProductService} from '../product/product.service';
import {RecentViewStore} from '../recent-view/recent-view.store';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {ProductTypeQuery} from './product-type-query.service';

@Injectable({providedIn: 'root'})
export class ProductTypeService {

  constructor(private productStore: ProductTypeStore,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              private http: HttpClient,
              private productTypeQuery: ProductTypeQuery,
              private recentViewQuery: RecentViewQuery,
              private recentViewStore: RecentViewStore,
              private productService: ProductService) {
  }

  get() {
    this.productStore.setLoading(true);
    this.http.get<ProductType[]>(`${this.CONSTANTS.apiPrefixV1}/productTypes`).pipe(
      finalize(() => this.productStore.setLoading(false)),
    ).subscribe(data => this.productStore.set(data));
  }

  setActive(productId: number | null) {
    if (productId != null) {
      const currentView = this.recentViewQuery.getEntity(productId);

      if (currentView == null) {
        this.recentViewStore.upsert(productId, ({productId: null, productTypeId: productId}));
      } else {
        this.recentViewStore.update(productId, ({productId: currentView.productId, productTypeId: productId}));
      }

      this.recentViewStore.setActive(productId);

      if (this.productTypeQuery.getEntity(productId).productIds === undefined) {
        this.productService.get(productId);
      }
    }
  }
}