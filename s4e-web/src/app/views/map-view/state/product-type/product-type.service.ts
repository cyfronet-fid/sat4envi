import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductTypeStore} from './product-type.store';
import {ProductType} from './product-type.model';
import {finalize} from 'rxjs/operators';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';
import {ProductService} from '../product/product.service';
import {RecentViewStore} from '../recent-view/recent-view.store';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {ProductTypeQuery} from './product-type.query';

@Injectable({providedIn: 'root'})
export class ProductTypeService {

  constructor(private productTypeStore: ProductTypeStore,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              private http: HttpClient,
              private productTypeQuery: ProductTypeQuery,
              private recentViewQuery: RecentViewQuery,
              private recentViewStore: RecentViewStore,
              private productService: ProductService) {
  }

  get() {
    this.productTypeStore.setLoading(true);
    this.http.get<ProductType[]>(`${this.CONSTANTS.apiPrefixV1}/productTypes`).pipe(
      finalize(() => this.productTypeStore.setLoading(false)),
    ).subscribe(data => this.productTypeStore.set(data));
  }

  setActive(productTypeId: number | null) {
    this.productTypeStore.setActive(productTypeId);
    if (productTypeId != null && this.recentViewQuery.getActiveId() !== productTypeId) {
      const currentView = this.recentViewQuery.getEntity(productTypeId);

      if (currentView == null) {
        this.recentViewStore.upsert(productTypeId, ({productId: null, productTypeId: productTypeId}));
      } else {
        this.recentViewStore.update(productTypeId, ({productId: currentView.productId, productTypeId: productTypeId}));
      }

      this.recentViewStore.setActive(productTypeId);

      if (this.productTypeQuery.getEntity(productTypeId).productIds === undefined) {
        this.productService.get(productTypeId);
      }
    }
    else if (this.recentViewQuery.hasEntity(productTypeId)){
      this.recentViewStore.setActive(null);
    }
  }
}
