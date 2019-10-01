import {Inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MapStore } from './map.store';
import {IConstants, S4E_CONSTANTS} from '../../../../app.constants';
import {MapQuery} from './map.query';
import {action} from '@datorama/akita';

@Injectable({ providedIn: 'root' })
export class MapService {

  constructor(private mapStore: MapStore,
              private mapQuery: MapQuery,
              private http: HttpClient,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) {
  }

  @action('toggleLegend')
  toggleLegend() {
    this.mapStore.update(state => ({...state, legendOpened: !state.legendOpened}));
  }
}
