import { Injectable } from '@angular/core';
import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import {createViewConfiguration, ViewConfiguration} from './view-configuration.model';
import {createProduct} from '../product/product.store';

export interface ViewConfigurationState extends EntityState<ViewConfiguration> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'ViewConfiguration', idKey: 'uuid' })
export class ViewConfigurationStore extends EntityStore<ViewConfigurationState, ViewConfiguration> {

  constructor() {
    super(createViewConfiguration());
  }

}

