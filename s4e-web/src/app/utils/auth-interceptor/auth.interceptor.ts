import {Injectable} from '@angular/core';
import {HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {SessionQuery} from '../../state/session/session.query';

@Injectable({
  providedIn: 'root',
})
export class AuthInterceptor implements HttpInterceptor {
  constructor(private sessionQuery: SessionQuery) {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    if (this.sessionQuery.isLoggedIn()) {
      req = req.clone({
        setHeaders: {'Authorization': `Bearer ${this.sessionQuery.getToken()}`},
      });
    }

    return next.handle(req);
  }
}
