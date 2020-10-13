import { SessionService } from './../../../state/session/session.service';
import { environment } from './../../../../environments/environment';
import { handleHttpRequest$ } from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {RegisterStore} from './register.store';
import {delay, finalize, tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import { RegisterFormState } from './register.model';

@Injectable({providedIn: 'root'})
export class RegisterService {

  constructor(
    private _store: RegisterStore,
    private _sessionService: SessionService,
    private _router: Router,
    private _http: HttpClient
  ) {}

  register(request: Partial<RegisterFormState>, recaptcha: string) {
    const captchaQuery = `g-recaptcha-response=${recaptcha}`;
    const url = `${environment.apiPrefixV1}/register?` + captchaQuery;
    this._http.post<RegisterFormState>(url, request)
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._router.navigate(['/'], { queryParamsHandling: 'merge' }))
      )
      .subscribe(
        () => this._router.navigate(['/register-confirmation'], { queryParamsHandling: 'merge' }),
        errorResponse => {
          const errorMessage = errorResponse.status === 400
            ? errorResponse.error
            : {__general__: [errorResponse.error]};
          this._store.setError(errorMessage);
        }
      );
  }
}
