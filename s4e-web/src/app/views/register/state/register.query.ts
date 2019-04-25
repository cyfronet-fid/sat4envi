import { Injectable } from '@angular/core';
import { Query } from '@datorama/akita';
import { RegisterStore } from './register.store';
import {RegisterState} from './register.model';

@Injectable({ providedIn: 'root' })
export class RegisterQuery extends Query<RegisterState> {

  constructor(protected store: RegisterStore) {
    super(store);
  }

}
