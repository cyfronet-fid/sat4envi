import { Injectable } from '@angular/core';
import { Store, StoreConfig } from '@datorama/akita';
import {RegisterState} from './register.model';

export function createInitialState(): RegisterState {
  return {
  };
}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Register' })
export class RegisterStore extends Store<RegisterState> {

  constructor() {
    super(createInitialState());
  }

}

