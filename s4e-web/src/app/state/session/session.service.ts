import { InvitationService, TOKEN_QUERY_PARAMETER } from './../../views/settings/people/state/invitation/invitation.service';
import {SessionQuery} from './session.query';
import {ERROR_INTERCEPTOR_SKIP_HEADER} from '../../utils/error-interceptor/error.helper';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SessionStore} from './session.store';
import {catchError, shareReplay, switchMap, tap} from 'rxjs/operators';
import {action} from '@datorama/akita';
import {LoginFormState, Session} from './session.model';
import {HTTP_401_UNAUTHORIZED, HTTP_404_BAD_REQUEST} from '../../errors/errors.model';
import {httpPostRequest$} from '../../common/store.util';
import {Observable, of} from 'rxjs';
import {NotificationService} from 'notifications';
import {IConfiguration} from '../../app.configuration';
import { Router, ParamMap, ActivatedRoute } from '@angular/router';
import {InjectorModule} from '../../common/injector.module';

export const BACK_LINK_QUERY_PARAM = 'back_link';

@Injectable({providedIn: 'root'})
export class SessionService {
  private CONFIG: IConfiguration;
  private _backLink: string;

  constructor(
    private _store: SessionStore,
    private _query: SessionQuery,
    private _notificationService: NotificationService,
    private _http: HttpClient,
    private _invitationService: InvitationService
  ) {}

  setBackLink(backLink: string) {
    this._backLink = backLink;
  }

  /**
   * This type of postinitialization is required to avoid circular
   * dependecy injection between S4EConfig and SessionService
   */
  init(config: IConfiguration) {
    this.CONFIG = config;
    this._store.update(_store => ({..._store, email: null}));
  }

  getProfile$(): Observable<Session | null> {
    const retValue$ = this._http.get<Session>(
      `${this.CONFIG.apiPrefixV1}/users/me`,
      {
        headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_401_UNAUTHORIZED.toString()}
      }).pipe(
      tap(profile => this._store.update({...profile})),
      catchError(() => of(null)),
      shareReplay(1)
    );

    retValue$.subscribe();

    return retValue$;
  }

  resetPassword(oldPassword: string, newPassword: string) {
    const url = '/api/v1/password-change';
    const request = {oldPassword, newPassword};
    return httpPostRequest$(this._http, url, request, this._store)
      .pipe(tap(() => this._notificationService.addGeneral({
        type: 'success',
        content: 'Hasło zostało zmienione'
      })));
  }

  @action('login')
  login(request: LoginFormState, activatedRoute: ActivatedRoute) {
    const url = `${this.CONFIG.apiPrefixV1}/login`;
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
          this._invitationService.confirm(token);
        })
      )
      .subscribe();
  }

  @action('logout')
  logout() {
    // IMPORTANT!!!
    // Router is injected, because injection in the APP_INITIALIZER creates circular-dependency
    const router: Router = InjectorModule.Injector.get(Router);
    this._http.post(`${this.CONFIG.apiPrefixV1}/logout`, {})
      .subscribe(() => {
        this._store.reset();
        router.navigate(['/login']);
      });
  }

  clearError() {
    this._store.setError(null);
  }

  private _navigateToApplication(): void {
    // IMPORTANT!!!
    // Router is injected, because injection in the APP_INITIALIZER creates circular-dependency
    const router: Router = InjectorModule.Injector.get(Router);
    router.navigate([this.getMainPageUrl()], {queryParamsHandling: 'merge'});
    delete(this._backLink);
  }

  private getMainPageUrl() {
    return !!this._backLink && this._backLink !== '' && this._backLink || '/';
  }
}
