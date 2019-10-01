import { Injectable } from '@angular/core';
import {ActiveState, EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import { ProductType } from './product-type.model';

export interface ProductTypeState extends EntityState<ProductType>, ActiveState {

}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'ProductType' })
export class ProductTypeStore extends EntityStore<ProductTypeState, ProductType> {
  constructor() {
    super();
  }
}

