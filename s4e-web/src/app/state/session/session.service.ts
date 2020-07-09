import { ProfileQuery } from 'src/app/state/profile/profile.query';
import {SessionQuery} from './session.query';
import {ERROR_INTERCEPTOR_CODES_TO_SKIP} from '../../utils/error-interceptor/error.helper';
import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {SessionStore} from './session.store';
import {finalize, switchMap, tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {action, resetStores} from '@datorama/akita';
import {LoginRequestResponse, LoginFormState} from './session.model';
import {S4eConfig} from '../../utils/initializer/config.service';
import {ProfileService} from '../profile/profile.service';
import {HTTP_404_BAD_REQUEST, HTTP_401_UNAUTHORIZED} from '../../errors/errors.model';
import {catchErrorAndHandleStore} from '../../common/store.util';
import ImageWrapper from 'ol/Image';
import { getImageXhr, ImageBase64 } from 'src/app/views/settings/manage-institutions/institution-form/files.utils';


@Injectable({providedIn: 'root'})
export class SessionService {
  private _backLink: string;

  constructor(private sessionStore: SessionStore,
              private sessionQuery: SessionQuery,
              private _profileQuery: ProfileQuery,
              private http: HttpClient,
              private router: Router,
              private profileService: ProfileService,
              private CONFIG: S4eConfig) {
  }

  set back_link(back_link: string) {
    this._backLink = back_link;
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
  login(formState: LoginFormState) {
    this.sessionStore.setLoading(true);
    const {login: email, password, ...x} = formState;
    const url = `${this.CONFIG.apiPrefixV1}/token`;
    this.http.post<LoginRequestResponse>(url, {email, password},
      {headers: {[ERROR_INTERCEPTOR_CODES_TO_SKIP]: `${HTTP_404_BAD_REQUEST},${HTTP_401_UNAUTHORIZED}`} })
      .pipe(
        catchErrorAndHandleStore(this.sessionStore),
        tap(data => this.setToken(data.token, data.email)),
        switchMap(data => this.profileService.get$()),
        finalize(() => this.sessionStore.setLoading(false))
      )
      .subscribe(data => {
        this._navigateToApplication();
      });
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

  private _navigateToApplication(): void {
    !!this._backLink && this._backLink !== ''
      ? this.router.navigateByUrl(this._backLink)
      : this.router.navigate(['/']);

    delete (this._backLink);
  }
}
