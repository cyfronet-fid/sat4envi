import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { ProductStore, ProductState } from './product-store.service';
import { Product } from './product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductQuery extends QueryEntity<ProductState, Product> {

  constructor(protected store: ProductStore) {
    super(store);
  }

}
