import {SessionQuery} from './session.query';
import {ERROR_INTERCEPTOR_CODES_TO_SKIP, ERROR_INTERCEPTOR_SKIP_HEADER} from '../../utils/error-interceptor/error.helper';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SessionStore} from './session.store';
import {catchError, finalize, shareReplay, switchMap, tap} from 'rxjs/operators';
import {action} from '@datorama/akita';
import {LoginFormState, Session} from './session.model';
import {HTTP_401_UNAUTHORIZED, HTTP_404_BAD_REQUEST} from '../../errors/errors.model';
import {catchErrorAndHandleStore, httpPostRequest$} from '../../common/store.util';
import {Observable, of} from 'rxjs';
import {NotificationService} from 'notifications';
import {IConfiguration} from '../../app.configuration';
import {Router} from '@angular/router';
import {InjectorModule} from '../../common/injector.module';


@Injectable({providedIn: 'root'})
export class SessionService {
  private CONFIG: IConfiguration;
  private router: Router;
  private _backLink: string;

  constructor(private store: SessionStore,
              private query: SessionQuery,
              private _notificationService: NotificationService,
              private http: HttpClient) {
  }

  setBackLink(back_link: string) {
    this._backLink = back_link;
  }

  /**
   * This type of postinitialization is required to avoid circular
   * dependecy injection between S4EConfig and SessionService
   */
  init(config: IConfiguration) {
    this.CONFIG = config;
    this.store.update(store => ({...store, email: null}));
  }

  getProfile$(): Observable<Session | null> {
    const retValue$ = this.http.get<Session>(
      `${this.CONFIG.apiPrefixV1}/users/me`,
      {
        headers: {[ERROR_INTERCEPTOR_SKIP_HEADER]: HTTP_401_UNAUTHORIZED.toString()}
      }).pipe(
      tap(profile => this.store.update({...profile})),
      catchError(() => of(null)),
      shareReplay(1)
    );

    retValue$.subscribe();

    return retValue$;
  }

  resetPassword(oldPassword: string, newPassword: string) {
    const url = '/api/v1/password-change';
    return httpPostRequest$(this.http, url, {oldPassword, newPassword}, this.store)
      .pipe(tap(() => this._notificationService.addGeneral({
        type: 'success',
        content: 'Hasło zostało zmienione'
      })));
  }

  @action('login')
  login(formState: LoginFormState) {
    this.store.setLoading(true);
    const {login: email, password, ...x} = formState;
    const url = `${this.CONFIG.apiPrefixV1}/login`;
    this.http.post(url, {email, password},
      {headers: {[ERROR_INTERCEPTOR_CODES_TO_SKIP]: `${HTTP_404_BAD_REQUEST},${HTTP_401_UNAUTHORIZED}`}})
      .pipe(
        catchErrorAndHandleStore(this.store),
        switchMap(data => this.getProfile$()),
        tap(() => this.store.update({email: formState.login})),
        finalize(() => this.store.setLoading(false))
      )
      .subscribe(data => this._navigateToApplication());
  }

  @action('logout')
  logout() {
    // We inject it here, because it can not be injected in the constructor
    // as this class is being injected at the APP_INITIALIZER time, and it creates
    // circular dependency
    const router: Router = InjectorModule.Injector.get(Router);
    this.http.post(`${this.CONFIG.apiPrefixV1}/logout`, {})
      .subscribe(() => {
        this.store.reset();
        router.navigate(['/login']);
      });
  }

  clearError() {
    this.store.setError(null);
  }

  private _navigateToApplication(): void {
    // We inject it here, because it can not be injected in the constructor
    // as this class is being injected at the APP_INITIALIZER time, and it creates
    // circular dependency
    const router: Router = InjectorModule.Injector.get(Router);

    !!this._backLink && this._backLink !== ''
      ? router.navigateByUrl(this._backLink)
      : router.navigate(['/']);

    delete (this._backLink);
  }
}
