import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LegendStore } from './legend.store';
import {COLLAPSED_LEGEND_LOCAL_STORAGE_KEY, Legend} from './legend.model';
import {LocalStorage} from '../../../../app.providers';

@Injectable({ providedIn: 'root' })
export class LegendService {
  constructor(private store: LegendStore,
              @Inject(LocalStorage) private storage: Storage,
              private http: HttpClient) {
  }

  set(legend: Legend) {
    this.store.update({legend});
  }

  toggleLegend() {
    this.store.update(state => {
      this.storage.setItem(COLLAPSED_LEGEND_LOCAL_STORAGE_KEY, JSON.stringify(!state.isOpen));
      return {...state, isOpen: !state.isOpen}
    });
  }
}
