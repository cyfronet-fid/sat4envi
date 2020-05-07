import { NotificationService } from 'notifications';
import { SessionQuery } from './session.query';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SessionStore} from './session.store';
import {finalize, switchMap, tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {action, resetStores, akitaConfig, persistState} from '@datorama/akita';
import {LoginRequestResponse} from './session.model';
import {S4eConfig} from '../../utils/initializer/config.service';
import {ProfileService} from '../profile/profile.service';

@Injectable({providedIn: 'root'})
export class SessionService {

  constructor(private sessionStore: SessionStore,
              private sessionQuery: SessionQuery,
              private http: HttpClient,
              private router: Router,
              private profileService: ProfileService,
              private CONFIG: S4eConfig,
              private _notificationService: NotificationService) {
  }

  init() {
    const token = localStorage.getItem('token');
    const email = localStorage.getItem('email') || '';

    if (token != null) {
      this.sessionStore.update(store => ({...store, token, email}));
    } else {
      this.sessionStore.update(store => ({...store, initialized: true, token: null, email: null}));
    }
  }

  setToken(token: string | null, email: string | null) {
    this.sessionStore.update(state => ({...state, token, email}));
    if (token == null) {
      localStorage.removeItem('token');
    } else {
      localStorage.setItem('token', token);
    }

    if (email == null) {
      localStorage.removeItem('email');
    } else {
      localStorage.setItem('email', email);
    }
  }

  @action('login')
  login(email: string, password: string) {
    this.sessionStore.setLoading(true);

    this.http.post<LoginRequestResponse>(`${this.CONFIG.apiPrefixV1}/login`, {email: email, password: password})
      .pipe(
        tap(data => this.setToken(data.token, data.email)),
        switchMap(data => this.profileService.get$()),
        finalize(() => this.sessionStore.setLoading(false))
      ).subscribe(data => {this.router.navigate(['/']);});
  }

  @action('logout')
  logout() {
    if (this.sessionQuery.isLoggedIn()) {
      this.setToken(null, null);
      resetStores({
        exclude: ['Notification']
      });
      this.router.navigateByUrl('login');
    }
  }
}
