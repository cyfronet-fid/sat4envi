import { REJECTION_QUERY_PARAMETER } from './../../views/settings/people/state/invitation.service';
import {SessionQuery} from './session.query';
import {ERROR_INTERCEPTOR_SKIP_HEADER} from '../../utils/error-interceptor/error.helper';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SessionStore} from './session.store';
import {catchError, shareReplay, switchMap, tap, filter} from 'rxjs/operators';
import {action} from '@datorama/akita';
import {LoginFormState, Session} from './session.model';
import {HTTP_401_UNAUTHORIZED, HTTP_404_BAD_REQUEST} from '../../errors/errors.model';
import {httpPostRequest$} from '../../common/store.util';
import {Observable, of} from 'rxjs';
import {NotificationService} from 'notifications';
import { Router, ParamMap, ActivatedRoute } from '@angular/router';
import { InvitationService, TOKEN_QUERY_PARAMETER } from 'src/app/views/settings/people/state/invitation.service';
import environment from 'src/environments/environment';
import { RemoteConfiguration } from 'src/app/utils/initializer/config.service';

export const BACK_LINK_QUERY_PARAM = 'back_link';

@Injectable({providedIn: 'root'})
export class SessionService {
  private router: Router;
  private _backLink: string;

  constructor(
    private _store: SessionStore,
    private _notificationService: NotificationService,
    private _http: HttpClient,
    private _invitationService: InvitationService,
    private _router: Router,
    private _remoteConfiguration: RemoteConfiguration
  ) {
    this._remoteConfiguration.isInitialized$
      .pipe(
        filter(isInitialized => isInitialized),
        switchMap(() => this.getProfile$())
      )
      .subscribe();
  }

  setBackLink(backLink: string) {
    this._backLink = backLink;
  }

  getProfile$(): Observable<Session | null> {
    const url = `${environment.apiPrefixV1}/users/me`;
    const skipUnauthorizedHeader = {headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_401_UNAUTHORIZED.toString()}};
    return this._http.get<Session>(url, skipUnauthorizedHeader)
      .pipe(
        tap(profile => this._store.update({...profile})),
        catchError(() => of(null)),
        shareReplay(1)
      );
  }

  resetPassword(oldPassword: string, newPassword: string) {
    const url = `${environment.apiPrefixV1}/password-change`;
    return httpPostRequest$(this._http, url, {oldPassword, newPassword}, this._store)
      .pipe(tap(() => this._notificationService.addGeneral({
        type: 'success',
        content: 'Hasło zostało zmienione'
      })));
  }

  @action('login')
  login(request: LoginFormState, activatedRoute: ActivatedRoute) {
    const url = `${environment.apiPrefixV1}/login`;
    return httpPostRequest$(this._http, url, request, this._store)
      .pipe(
        switchMap(data => this.getProfile$()),
        tap(() => this._store.update({email: request.email})),
        tap(() => this._navigateToApplication()),
        switchMap(() => activatedRoute.queryParamMap),
        tap((params) => {
          if (!params.has(TOKEN_QUERY_PARAMETER)) {
            return;
          }

          const token = params.get(TOKEN_QUERY_PARAMETER);
          this._invitationService.accept(token);
        })
      )
      .subscribe();
  }

  @action('logout')
  logout() {
    this._http.post(`${environment.apiPrefixV1}/logout`, {})
      .subscribe(() => {
        this._store.reset();
        this._router.navigate(['/login']);
      });
  }

  clearError() {
    this._store.setError(null);
  }

  private _navigateToApplication(): void {
    !!this._backLink && this._backLink !== ''
      ? this._router.navigateByUrl(this._backLink)
      : this._router.navigate(['/map/products']);

    delete (this._backLink);
  }
}
