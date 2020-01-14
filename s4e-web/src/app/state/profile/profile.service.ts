import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import { ProfileStore } from './profile.store';
import {Observable, of, pipe} from 'rxjs';
import {Profile} from './profile.model';
import {catchError, shareReplay, tap} from 'rxjs/operators';
import {SessionQuery} from '../session/session.query';
import {SessionService} from '../session/session.service';

@Injectable({ providedIn: 'root' })
export class ProfileService {

  constructor(private store: ProfileStore,
              private sessionQuery: SessionQuery,
              private http: HttpClient) {
  }

  get$(): Observable<Profile|null> {
    if(localStorage.getItem('token') == null) {
      return of(null);
    }

    const r = this.http.get<Profile>('/api/v1/users/me').pipe(tap(
      profile => this.store.update({...profile, loggedIn: true})
    ), catchError(() => {
      localStorage.removeItem('token');
      localStorage.removeItem('email');
      return of(null);
    }), shareReplay(1));

    r.subscribe();

    return r;
  }
}
