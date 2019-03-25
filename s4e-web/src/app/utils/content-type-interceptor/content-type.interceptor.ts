import {Injectable} from '@angular/core';
import {HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ContentTypeInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    req = req.clone({
      responseType: 'json',
    });

    return next.handle(req);
  }
}
