import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { InstitutionStore, InstitutionState } from './institution.store';
import { Institution } from './institution.model';

@Injectable({
  providedIn: 'root'
})
export class InstitutionQuery extends QueryEntity<InstitutionState, Institution> {

  constructor(protected store: InstitutionStore) {
    super(store);
  }
}
