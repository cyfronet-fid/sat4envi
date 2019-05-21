import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {ProductTypeStore, ProductTypeState} from './product-type.store';
import {ProductType} from './product-type.model';
import {ProductQuery} from '../product/product-query.service';

@Injectable({
  providedIn: 'root'
})
export class ProductTypeQuery extends QueryEntity<ProductTypeState, ProductType> {
  constructor(protected store: ProductTypeStore, private productQuery: ProductQuery) {
    super(store);
  }
}
