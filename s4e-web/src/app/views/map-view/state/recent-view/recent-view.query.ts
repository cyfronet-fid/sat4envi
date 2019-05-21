import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {RecentViewState, RecentViewStore} from './recent-view.store';
import {ICompleteRecentView, RecentView} from './recent-view.model';
import {combineLatest, Observable, of} from 'rxjs';
import {ProductTypeQuery} from '../product-type/product-type.query';
import {ProductQuery} from '../product/product-query.service';
import {distinctUntilChanged, filter, map, mergeMap, tap, withLatestFrom} from 'rxjs/operators';
import {Product} from '../product/product.model';
import {IUILayer} from '../common.model';

@Injectable({
  providedIn: 'root'
})
export class RecentViewQuery extends QueryEntity<RecentViewState, RecentView> {

  constructor(protected store: RecentViewStore, private productTypeQuery: ProductTypeQuery, private productQuery: ProductQuery) {
    super(store);
  }

  selectViewsWithData(): Observable<ICompleteRecentView[]> {
    return this.selectAll().pipe(
      map(views => views.map(view => ({
        ...view,
        activeProduct: this.productQuery.getEntity(view.productId),
        activeProductType: this.productTypeQuery.getEntity(view.productTypeId)
      })))
    );
  }

  selectActiveViewProducts(): Observable<Product[]> {
    return this.selectActive().pipe(
      mergeMap(view => view != null ? this.productTypeQuery.selectEntity(view.productTypeId) : of(null)),
      mergeMap(product => product ? this.productQuery.selectMany(product.productIds) : of([])),
    );
  }

  selectActiveProduct(): Observable<Product> {
    return this.selectActive().pipe(
      mergeMap(view => (view != null && view.productId) ? this.productQuery.selectEntity(view.productId) : of(null)),
      distinctUntilChanged()
    );
  }


  selectProductsAsIUILayers(): Observable<IUILayer[]> {
    return combineLatest(this.productTypeQuery.selectAll(),
      this.selectActive().pipe(map(active => active != null ? active.productTypeId : null)))
      .pipe(
        map(([items, activeProductTypeId]) => items.map(productType =>
          ({
            cid: productType.id,
            caption: productType.name,
            active: productType.id === activeProductTypeId
          })))
      );
  }
}
