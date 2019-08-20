import {Injectable} from '@angular/core';
import {Query} from '@datorama/akita';
import {ActivateStore} from './activate.store';
import {ActivateState} from './activate.model';

@Injectable({providedIn: 'root'})
export class ActivateQuery extends Query<ActivateState> {

  constructor(protected store: ActivateStore) {
    super(store);
  }

}
