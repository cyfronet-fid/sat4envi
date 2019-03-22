import { Injectable } from '@angular/core';
import { Query } from '@datorama/akita';
import { MapStore } from './map.store';
import {MapState} from './map.model';

@Injectable({ providedIn: 'root' })
export class MapQuery extends Query<MapState> {
  constructor(protected store: MapStore) {
    super(store);
  }

}
