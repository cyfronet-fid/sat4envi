import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { ProductStore, ProductState } from './product.store';
import { Product } from './product.model';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map} from 'rxjs/operators';
// import {combineLatest, Observable, of} from 'rxjs';
// import {ICompleteRecentView} from '../recent-view/recent-view.model';
// import {distinctUntilChanged, map, mergeMap} from 'rxjs/operators';
// import {IUILayer} from '../common.model';

@Injectable({
  providedIn: 'root'
})
export class ProductQuery extends QueryEntity<ProductState, Product> {

  constructor(protected store: ProductStore) {
    super(store);
  }
  //
  // selectViewsWithData(): Observable<ICompleteRecentView[]> {
  //   return this.selectAll().pipe(
  //     map(views => views.map(view => ({
  //       ...view,
  //       activeProduct: this.productQuery.getEntity(view.productId),
  //       activeProductType: this.productTypeQuery.getEntity(view.productTypeId)
  //     })))
  //   );
  // }
  //
  // selectActiveViewProducts(): Observable<Product[]> {
  //   return this.selectActive().pipe(
  //     mergeMap(view => view != null ? this.productTypeQuery.selectEntity(view.productTypeId) : of(null)),
  //     mergeMap(product => product ? this.productQuery.selectMany(product.productIds) : of([])),
  //   );
  // }
  //
  // selectActiveProduct(): Observable<Product> {
  //   return this.selectActive().pipe(
  //     mergeMap(view => (view != null && view.productId) ? this.productQuery.selectEntity(view.productId) : of(null)),
  //     distinctUntilChanged()
  //   );
  // }
  //
  //
}
