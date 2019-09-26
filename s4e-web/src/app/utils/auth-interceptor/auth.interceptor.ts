import {Injectable} from '@angular/core';
import {HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {SessionQuery} from '../../state/session/session.query';
import {OAuthService} from 'angular-oauth2-oidc';

@Injectable({
  providedIn: 'root',
})
export class AuthInterceptor implements HttpInterceptor {
  constructor(private sessionQuery: SessionQuery,
              private oauthService: OAuthService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    if (req.url.endsWith('/token')) {
      // TODO: Workaround: angular-oauth2-oidc does not set Basic Authentication header
      //       in refreshToken(). We require it.
      const auth = btoa('s4e' + ':' + 'secret');
      req = req.clone({
        setHeaders: {
          'Authorization': `Basic ${auth}`
        }
      });
    } else if (this.oauthService.hasValidAccessToken()) {
      req = req.clone({
        setHeaders: {'Authorization': `Bearer ${this.oauthService.getAccessToken()}`},
      });
    }

    return next.handle(req);
  }
}
