import {Injectable} from '@angular/core';
import {EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {Institution} from './institution.model';

export interface InstitutionState extends EntityState<Institution> {}

@Injectable({providedIn: 'root'})
@StoreConfig({
  name: 'Institution',
  idKey: 'slug',
  cache: {ttl: 60}
})
export class InstitutionStore extends EntityStore<InstitutionState, Institution, string> {
  constructor() {
    super();
  }
}

