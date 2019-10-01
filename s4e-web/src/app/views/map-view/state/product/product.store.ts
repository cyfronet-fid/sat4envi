import { Injectable } from '@angular/core';
import {ActiveState, EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import { Product } from './product.model';

export interface ProductState extends EntityState<Product>, ActiveState {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Product', idKey: 'id' })
export class ProductStore extends EntityStore<ProductState, Product> {

  constructor() {
    super();
  }
}

