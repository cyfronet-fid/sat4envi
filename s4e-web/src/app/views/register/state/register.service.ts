import { httpPostRequest$ } from 'src/app/common/store.util';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {RegisterStore} from './register.store';
import {delay, finalize, tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {S4eConfig} from '../../../utils/initializer/config.service';
import { RegisterFormState } from './register.model';

@Injectable({providedIn: 'root'})
export class RegisterService {

  constructor(private registerStore: RegisterStore,
              private router: Router,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  register(user: RegisterFormState) {
    const {recaptcha, passwordRepeat, ...userData} = user;
    const url = `${this.CONFIG.apiPrefixV1}/register?g-recaptcha-response=${recaptcha}`;
    httpPostRequest$(this.http, url, userData, this.registerStore)
      .pipe(tap(() => this.router.navigate(['/'])))
      .subscribe(() => {}, errorResponse => {
        if (errorResponse.status === 400) {
          this.registerStore.setError(errorResponse.error);
        } else {
          this.registerStore.setError({__general__: [errorResponse.error]});
        }
      });
  }
}
