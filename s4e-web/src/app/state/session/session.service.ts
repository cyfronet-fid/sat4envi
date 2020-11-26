import {handleHttpRequest$} from 'src/app/common/store.util';
import {ERROR_INTERCEPTOR_SKIP_HEADER} from '../../utils/error-interceptor/error.helper';
import {SessionStore} from './session.store';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import {action} from '@datorama/akita';
import {LoginFormState, Session} from './session.model';
import {HTTP_401_UNAUTHORIZED} from '../../errors/errors.model';
import {Observable, of} from 'rxjs';
import {NotificationService} from 'notifications';
import {Router} from '@angular/router';
import environment from 'src/environments/environment';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

export const BACK_LINK_QUERY_PARAM = 'back_link';

@Injectable({providedIn: 'root'})
export class ProfileLoaderService {
  constructor(
    private _store: SessionStore,
    private _http: HttpClient,
  ) {
  }

  loadProfile$(): Observable<Session | null> {
    const url = `${environment.apiPrefixV1}/users/me`;
    const skipUnauthorizedHeader = {headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_401_UNAUTHORIZED.toString()}};
    return this._http.get<Session>(url, skipUnauthorizedHeader)
      .pipe(
        tap(profile => this._store.update({...profile})),
        catchError(() => of(null)),
      );
  }
}

@Injectable({providedIn: 'root'})
export class SessionService {
  private _backLink: string;

  constructor(
    private _store: SessionStore,
    private _notificationService: NotificationService,
    private _http: HttpClient,
    private _router: Router,
    private _profileLoaderService: ProfileLoaderService
  ) {
  }

  loadProfile$() {
    return this._profileLoaderService.loadProfile$();
  }

  setBackLink(backLink: string) {
    this._backLink = backLink;
  }

  resetPassword(oldPassword: string, newPassword: string) {
    const url = `${environment.apiPrefixV1}/password-change`;
    return this._http.post(url, {oldPassword, newPassword})
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._notificationService.addGeneral({
          type: 'success',
          content: 'Hasło zostało zmienione'
        }))
      );
  }

  @action('login')
  login$(request: LoginFormState) {
    const url = `${environment.apiPrefixV1}/login`;
    return this._http.post<LoginFormState>(url, request)
      .pipe(
        handleHttpRequest$(this._store),
        switchMap(data => this._profileLoaderService.loadProfile$()),
        tap(() => this._store.update({email: request.email})),
        tap(async () => await this._navigateToApplication())
      );
  }

  @action('logout')
  logout() {
    this._http.post(`${environment.apiPrefixV1}/logout`, {})
      .subscribe(async () => {
        this._store.reset();
        await this._router.navigate(['/login']);
      });
  }

  removeAccount$(email: string, password: string) {
    const url = `${environment.apiPrefixV1}/users/forget-me`;
    return this._http.post(url, {email, password})
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this.logout())
      );
  }

  getJwtToken$(request: LoginFormState) {
    const url = `${environment.apiPrefixV1}/token`;
    return this._http.post<{ email: string, token: string }>(url, request)
      .pipe(map(response => response.token));
  }

  clearError() {
    this._store.setError(null);
  }

  private async _navigateToApplication(): Promise<void> {
    await (
      !!this._backLink && this._backLink !== ''
        ? this._router.navigateByUrl(this._backLink)
        : this._router.navigate(['/map/products'])
    );

    delete (this._backLink);
  }
}
