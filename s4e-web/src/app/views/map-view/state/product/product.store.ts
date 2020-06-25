import {Injectable} from '@angular/core';
import {EntityStore, EntityUIStore, StoreConfig} from '@datorama/akita';
import {createProductState, Product, ProductState, ProductUIState} from './product.model';


@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Product'})
export class ProductStore extends EntityStore<ProductState, Product> {
  public readonly ui: EntityUIStore<ProductUIState>;

  constructor() {
    super(createProductState());
    this.createUIStore().setInitialEntityState({isLoading: false, isFavouriteLoading: false});
  }
}

