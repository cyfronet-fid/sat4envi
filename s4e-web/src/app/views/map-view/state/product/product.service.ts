import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ProductStore} from './product.store';
import {Product} from './product.model';
import {finalize} from 'rxjs/operators';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';
import {GranuleService} from '../granule/granule.service';
import {RecentViewStore} from '../recent-view/recent-view.store';
import {RecentViewQuery} from '../recent-view/recent-view.query';
import {ProductQuery} from './product.query';

@Injectable({providedIn: 'root'})
export class ProductService {

  constructor(private productStore: ProductStore,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              private http: HttpClient,
              private productQuery: ProductQuery,
              private recentViewQuery: RecentViewQuery,
              private recentViewStore: RecentViewStore,
              private granuleService: GranuleService) {
  }

  get() {
    this.productStore.setLoading(true);
    this.http.get<Product[]>(`${this.CONSTANTS.apiPrefixV1}/products`).pipe(
      finalize(() => this.productStore.setLoading(false)),
    ).subscribe(data => this.productStore.set(data));
  }

  setActive(productId: number | null) {
    if (productId != null) {
      const currentView = this.recentViewQuery.getEntity(productId);

      if (currentView == null) {
        this.recentViewStore.upsert(productId, ({granuleId: null, productId: productId}));
      } else {
        this.recentViewStore.update(productId, ({granuleId: currentView.granuleId, productId: productId}));
      }

      this.recentViewStore.setActive(productId);

      if (this.productQuery.getEntity(productId).granuleIds === undefined) {
        this.granuleService.get(productId);
      }
    }
  }
}
