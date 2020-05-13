import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {catchError, filter, finalize, map, pairwise} from 'rxjs/operators';
import {Router, RoutesRecognized} from '@angular/router';
import {
  HTTP_101_TIMEOUT,
  HTTP_401_UNAUTHORIZED,
  HTTP_403_FORBIDDEN,
  HTTP_404_BAD_REQUEST,
  HTTP_404_NOT_FOUND,
  HTTP_500_INTERNAL_SERVER_ERROR,
  HTTP_502_BAD_GATEWAY
} from '../../errors/errors.model';
import {HttpErrorHelper} from './error.helper';
import {NotificationService} from 'notifications';


@Injectable({
  providedIn: 'root',
})
export class ErrorInterceptor implements HttpInterceptor {
  private _backLink: string;

  constructor(
    private _router: Router,
    private _notificationService: NotificationService
  ) {
    this._router.events
      .pipe(
        filter(event => event instanceof RoutesRecognized),
        pairwise(),
        map((event: [RoutesRecognized, RoutesRecognized]) => event[0].url)
      )
      .subscribe((lastUrl: string) => this._backLink = '/' + lastUrl);
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    request = HttpErrorHelper.addDefaultHeaders(request);

    return next.handle(request)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (HttpErrorHelper.hasMutuallyExclusiveOptions(request)) {
            console.error('HTTP request has mutually exclusive options, make sure that only one skip header is provided');
          } else if (!HttpErrorHelper.skipHandling(error, request)) {
            if (HttpErrorHelper.isClientSideError(error)) {
              // this is client side unexpected error, generally this should not happen
              console.error(error.message);
            } else if (HttpErrorHelper.isServerSideError(error)) {
              return throwError(error).pipe(finalize(() => this._handleServerError(error)));
            }
          }
          return throwError(error);
        })
      );
  }

  private _handleServerError = (error: HttpErrorResponse) => {
    switch (error.status) {
      case HTTP_403_FORBIDDEN:
      case HTTP_401_UNAUTHORIZED:
        this._notificationService.addGeneral({
          type: 'error',
          content: 'Wystąpił błąd: Nie jesteś zalogowany lub twoja sesja się przedawniła'
        });
        this._router.navigate(['login'], {queryParams: {back_link: this._backLink}});
        break;
      case HTTP_101_TIMEOUT:
      case HTTP_404_BAD_REQUEST:
        this._notificationService.addGeneral({
          type: 'error',
          content: 'Wystąpił błąd: Nieprawidłowe zapytanie lub zbyt długi czas oczekiwania na odpowiedź serwera'
        });
        break;
      case HTTP_404_NOT_FOUND:
        this._router.navigate(['errors', error.status]);
        break;
      case HTTP_502_BAD_GATEWAY:
      case HTTP_500_INTERNAL_SERVER_ERROR:
        this._router.navigate(['errors', error.status], {queryParams: {back_link: this._backLink}});
        break;
      default:
        this._notificationService.addGeneral({
          type: 'error',
          content: `Wystąpił nieznany błąd: ${error.status}, w przypadku dalszego występowania skontaktuj się z administratorem`
        });
        break;
    }
  };
}
