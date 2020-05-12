import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import { ProfileStore } from './profile.store';
import {Observable, of, pipe, throwError} from 'rxjs';
import {Profile} from './profile.model';
import {catchError, shareReplay, tap} from 'rxjs/operators';
import {SessionQuery} from '../session/session.query';
import { catchErrorAndHandleStore } from 'src/app/common/store.util';

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

  resetPassword(oldPassword: string, newPassword: string): void {
    this.store.setLoading(true);
    this.http.post('/api/v1/password-change', {oldPassword, newPassword})
      .pipe(catchErrorAndHandleStore(this.store))
      .subscribe(() => this.store.setLoading(false));
  }
}
