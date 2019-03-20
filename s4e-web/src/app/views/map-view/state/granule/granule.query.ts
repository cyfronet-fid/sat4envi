import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { GranuleStore, GranuleState } from './granule.store';
import { Granule } from './granule.model';

@Injectable({
  providedIn: 'root'
})
export class GranuleQuery extends QueryEntity<GranuleState, Granule> {

  constructor(protected store: GranuleStore) {
    super(store);
  }

}
