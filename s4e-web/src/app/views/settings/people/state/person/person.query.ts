import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';
import { PersonStore, PersonState } from './person.store';
import { Person } from './person.model';

@Injectable({
  providedIn: 'root'
})
export class PersonQuery extends QueryEntity<PersonState, Person> {

  constructor(protected store: PersonStore) { super(store); }

  selectAllWithInvitations$() {

  }
}
