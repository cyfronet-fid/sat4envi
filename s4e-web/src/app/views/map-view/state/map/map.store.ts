import {Inject, Injectable} from '@angular/core';
import {Store, StoreConfig} from '@datorama/akita';
import {createInitialState, MapState} from './map.model';
import {LocalStorage} from '../../../../app.providers';

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Map'})
export class MapStore extends Store<MapState> {
  constructor(@Inject(LocalStorage) storage: Storage) {
    super(createInitialState(storage));
  }
}

