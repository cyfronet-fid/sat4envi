import { Injectable } from '@angular/core';
import {ActiveState, EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import { Granule } from './granule.model';

export interface GranuleState extends EntityState<Granule> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Granule', idKey: 'id' })
export class GranuleStore extends EntityStore<GranuleState, Granule> {

  constructor() {
    super();
  }

}

