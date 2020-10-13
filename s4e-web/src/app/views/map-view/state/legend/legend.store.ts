import {Inject, Injectable} from '@angular/core';
import {Store, StoreConfig} from '@datorama/akita';
import {COLLAPSED_LEGEND_LOCAL_STORAGE_KEY, LegendState} from './legend.model';
import {LocalStorage} from '../../../../app.providers';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Legend' })
export class LegendStore extends Store<LegendState> {
  constructor(@Inject(LocalStorage) storage: Storage) {
    super({
      legend: null,
      isOpen: JSON.parse(storage.getItem(COLLAPSED_LEGEND_LOCAL_STORAGE_KEY) || 'false')
    });
  }
}

