import {Injectable} from '@angular/core';
import {Store, StoreConfig} from '@datorama/akita';
import {ActivateState, State} from './activate.model';export function createInitialState(): ActivateState {
  return {
    state: 'activating'
  };
}



@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Activate'})
export class ActivateStore extends Store<ActivateState> {

  constructor() {
    super(createInitialState());
  }

  setState(newState: State) {
    this.update(state => ({...state, state: newState}));
  }
}
