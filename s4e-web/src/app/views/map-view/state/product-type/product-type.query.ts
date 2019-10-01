import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {ProductTypeStore, ProductTypeState} from './product-type.store';
import {ProductType} from './product-type.model';
import {ProductQuery} from '../product/product.query';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map, switchMap} from 'rxjs/operators';
import {UIOverlay} from '../overlay/overlay.model';
import {Product} from '../product/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductTypeQuery extends QueryEntity<ProductTypeState, ProductType> {
  constructor(protected store: ProductTypeStore, private productQuery: ProductQuery) {
    super(store);
  }

  public selectAllAsUILayer(): Observable<IUILayer[]> {
    return combineLatest(this.selectAll(), this.selectActiveId()).pipe(
      map(([productTypes, activeId]) => productTypes.map(pt => ({cid: pt.id, caption: pt.name, active: pt.id === activeId})))
    );
  }

  selectAllProducts(): Observable<Product[]> {
    return this.selectActive().pipe(switchMap(active => {
      if (active == null) { return [[]] }

      return this.productQuery.selectMany(active.productIds);
    }));
  }
}
