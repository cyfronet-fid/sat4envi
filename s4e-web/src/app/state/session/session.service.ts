import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {SessionStore} from './session.store';
import {finalize} from 'rxjs/operators';
import {Router} from '@angular/router';
import {action, resetStores} from '@datorama/akita';
import {IConstants, S4E_CONSTANTS} from '../../app.constants';
import {OAuthService} from 'angular-oauth2-oidc';
import {authConfig} from '../../oauth.config';
import {from} from 'rxjs';

@Injectable({providedIn: 'root'})
export class SessionService {

  constructor(private sessionStore: SessionStore,
              private http: HttpClient,
              private router: Router,
              private oauthService: OAuthService,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants) {
    this.oauthService.configure(authConfig);
    this.setupTokenRefresh();
    // this.oauthService.setupAutomaticSilentRefresh();
  }

  init() {
  }


  @action('login')
  login(email: string, password: string) {
    this.sessionStore.setLoading(true);

    from(this.oauthService.fetchTokenUsingPasswordFlow(email, password))
      .pipe(
        finalize(() => {
            this.sessionStore.setLoading(false);
          }
        )
      )
      .subscribe(data => {
        this.sessionStore.update(store => ({logged: true}));
        this.router.navigate(['/']);
      });
  }

  @action('logout')
  logout() {
    this.oauthService.logOut(true);
    this.sessionStore.update(store => ({logged: false}));
    resetStores();
    this.router.navigate(['/login']);
  }

  private setupTokenRefresh() {
    this.oauthService.events.subscribe(e => {
      if (e.type === 'token_expires') {
        this.oauthService.refreshToken()
          .then(value => console.log('Token refreshed'))
        ;
      }
    });
    if (this.oauthService.hasValidAccessToken()) {
      this.oauthService.refreshToken();
    }
  }
}
