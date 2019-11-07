import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {ProductTypeStore, ProductTypeState} from './product-type.store';
import {ProductType} from './product-type.model';
import {ProductQuery} from '../product/product.query';
import {combineLatest, Observable} from 'rxjs';
import {IUILayer} from '../common.model';
import {map} from 'rxjs/operators';

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
}
