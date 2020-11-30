import {Inject, Injectable} from '@angular/core';
import {EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {HC_LOCAL_STORAGE_KEY, LARGE_FONT_LOCAL_STORAGE_KEY, ViewConfiguration} from './view-configuration.model';
import {LocalStorage} from '../../../../app.providers';

export interface ViewConfigurationState extends EntityState<ViewConfiguration> {
  highContrast: boolean;
  largeFont: boolean;
}

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'ViewConfiguration', idKey: 'uuid'})
export class ViewConfigurationStore extends EntityStore<ViewConfigurationState, ViewConfiguration> {
  constructor(@Inject(LocalStorage) storage: Storage) {
    super({
      loading: false,
      highContrast: JSON.parse(storage.getItem(HC_LOCAL_STORAGE_KEY) || 'false'),
      largeFont: JSON.parse(storage.getItem(LARGE_FONT_LOCAL_STORAGE_KEY) || 'false')
    });
  }
}

