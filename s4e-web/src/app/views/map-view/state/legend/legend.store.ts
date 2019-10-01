import {Injectable} from '@angular/core';
import {Store, StoreConfig} from '@datorama/akita';
import {createInitialState, LegendState} from './legend.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Legend' })
export class LegendStore extends Store<LegendState> {

  constructor() {
    super(createInitialState());
  }

}

