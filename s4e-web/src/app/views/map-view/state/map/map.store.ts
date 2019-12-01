import {Injectable} from '@angular/core';
import {Store, StoreConfig} from '@datorama/akita';
import {createInitialState, MapState} from './map.model';

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Map'})
export class MapStore extends Store<MapState> {
  constructor() {
    super(createInitialState());
  }
}

