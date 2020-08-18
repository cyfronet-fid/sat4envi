import { Injectable } from '@angular/core';
import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';
import { Person } from './person.model';

export interface PersonState extends EntityState<Person> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Person', idKey: 'email'})
export class PersonStore extends EntityStore<PersonState, Person, string> {

  constructor() {
    super();
  }

}

