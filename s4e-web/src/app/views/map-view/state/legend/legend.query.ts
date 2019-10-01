import { Injectable } from '@angular/core';
import { Query } from '@datorama/akita';
import { LegendStore} from './legend.store';
import {LegendState} from './legend.model';

@Injectable({ providedIn: 'root' })
export class LegendQuery extends Query<LegendState> {

  constructor(protected store: LegendStore) {
    super(store);
  }

  selectLegend() {
    return this.select('legend');
  }
}
