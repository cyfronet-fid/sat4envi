import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { ProductStore, ProductState } from './product.store';
import { Product } from './product.model';
import {GranuleQuery} from '../granule/granule.query';

@Injectable({
  providedIn: 'root'
})
export class ProductQuery extends QueryEntity<ProductState, Product> {
  constructor(protected store: ProductStore, private granuleQuery: GranuleQuery) {
    super(store);
  }
}
