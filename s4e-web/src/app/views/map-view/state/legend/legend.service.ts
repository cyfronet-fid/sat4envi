import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LegendStore } from './legend.store';
import {Legend} from './legend.model';

@Injectable({ providedIn: 'root' })
export class LegendService {
  constructor(private store: LegendStore,
              private http: HttpClient) {
  }

  set(legend: Legend) {
    this.store.update({legend});
  }

  toggleLegend() {
    this.store.update(state => ({...state, isOpen: !state.isOpen}));
  }
}
