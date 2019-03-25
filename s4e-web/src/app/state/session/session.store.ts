import { Injectable } from '@angular/core';
import {EntityState, EntityStore, Store, StoreConfig} from '@datorama/akita';
import {createSession, Session} from './session.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Session' })
export class SessionStore extends Store<Session> {

  constructor() {
    super(createSession({}));
  }

}

