import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {RecentViewState, RecentViewStore} from './recent-view.store';
import {ICompleteRecentView, RecentView} from './recent-view.model';
import {Observable, of} from 'rxjs';
import {ProductTypeQuery} from '../product-type/product-type-query.service';
import {ProductQuery} from '../product/product-query.service';
import {distinctUntilChanged, filter, map, mergeMap, tap} from 'rxjs/operators';
import {Product} from '../product/product.model';

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
      filter(view => view != null),
      mergeMap(view => this.productTypeQuery.selectEntity(view.productTypeId)),
      mergeMap(product => product ? this.productQuery.selectMany(product.productIds) : of([])),
    );
  }

  selectActiveProduct(): Observable<Product> {
    return this.selectActive().pipe(
      filter(view => view != null),
      mergeMap(view => view.productId ? this.productQuery.selectEntity(view.productId) : of(null)),
      distinctUntilChanged()
    );
  }
}
