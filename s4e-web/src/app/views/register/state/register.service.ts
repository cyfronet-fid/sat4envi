import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {RegisterStore} from './register.store';
import {of} from 'rxjs';
import {delay, finalize} from 'rxjs/operators';
import {Router} from '@angular/router';
import {IConstants, S4E_CONSTANTS} from '../../../app.constants';
import * as CONSTANTS from 'constants';

@Injectable({providedIn: 'root'})
export class RegisterService {

  constructor(private registerStore: RegisterStore,
              private router: Router,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              private http: HttpClient) {
  }

  /**
   * @param email
   * @param password
   */
  register(email: string, password: string) {
    this.registerStore.setLoading(true);
    this.http.post(`${this.CONSTANTS.apiPrefixV1}/register`, {email, password})
      .pipe(delay(1000), finalize(() => this.registerStore.setLoading(false)))
    .subscribe(data => {
      this.router.navigate(['/']);
    }, errorResponse => {
      if (errorResponse.status === 400) {
        this.registerStore.setError(errorResponse.error);
      } else {
        this.registerStore.setError({__general__: [errorResponse.error]});
      }
    });
  }
}
