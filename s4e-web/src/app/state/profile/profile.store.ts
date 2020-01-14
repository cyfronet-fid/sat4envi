import {Injectable} from '@angular/core';
import {Store, StoreConfig} from '@datorama/akita';
import {createInitialState, ProfileState} from './profile.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Profile' })
export class ProfileStore extends Store<ProfileState> {

  constructor() {
    super(createInitialState());
  }

}

