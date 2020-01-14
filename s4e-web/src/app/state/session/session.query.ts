import {Injectable} from '@angular/core';
import {Query} from '@datorama/akita';
import {SessionStore} from './session.store';
import {Session} from './session.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SessionQuery extends Query<Session> {

  constructor(protected store: SessionStore) {
    super(store);
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('token') != null;
  }

  isLoggedIn$(): Observable<boolean> {
    return this.select(state => state.token != null);
  }

  getToken(): string|null {
    return localStorage.getItem('token');
  }

  isInitialized() {
    return this.getValue().initialized;
  }
}
