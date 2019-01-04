import {Injectable} from '@angular/core';
import {HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';

import {LoginService} from '../auth/login.service';

@Injectable({
  providedIn: 'root',
})
export class AuthInterceptor implements HttpInterceptor {
  constructor(private loginService: LoginService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const user = this.loginService.currentUserValue;

    if (user && user.token) {
      req = req.clone({
        setHeaders: {'Authorization': `Bearer ${user.token}`},
      });
    }

    return next.handle(req);
  }
}
