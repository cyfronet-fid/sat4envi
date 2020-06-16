import { NotificationService } from 'notifications';
import { httpPostRequest$ } from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {ProfileStore} from './profile.store';
import {Observable, of} from 'rxjs';
import {Profile} from './profile.model';
import { catchError, shareReplay, tap, finalize } from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';

@Injectable({providedIn: 'root'})
export class ProfileService {

  constructor(
    private store: ProfileStore,
    private http: HttpClient,
    private _notificationService: NotificationService
  ) {}

  get$(): Observable<Profile | null> {
    if (localStorage.getItem('token') == null) {
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

  resetPassword(oldPassword: string, newPassword: string) {
    const url = '/api/v1/password-change';
    return httpPostRequest$(this.http, url, {oldPassword, newPassword}, this.store)
      .pipe(tap(() => this._notificationService.addGeneral({
        type: 'success',
        content: 'Hasło zostało zmienione'
      })));
  }
}
