import {Inject, Injectable} from '@angular/core';
import {action} from '@datorama/akita';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';

import {IConstants, S4E_CONSTANTS} from '../../../app.constants';
import {ActivateStore} from './activate.store';
import {Router} from '@angular/router';
import {delay, finalize} from 'rxjs/operators';

@Injectable({providedIn: 'root'})
export class ActivateService {

  constructor(private activateStore: ActivateStore,
              @Inject(S4E_CONSTANTS) private CONSTANTS: IConstants,
              private router: Router,
              private http: HttpClient) {
  }

  @action('activate')
  activate(token: string) {
    this.activateStore.setError(null);
    this.activateStore.setLoading(true);
    this.activateStore.setState('activating');

    this.http.post(`${this.CONSTANTS.apiPrefixV1}/confirm-email`, {}, {params: {token}})
      .pipe(delay(1000), finalize(() => this.activateStore.setLoading(false)))
      .subscribe(
        () => {
          this.router.navigate(['/login']);
        },
        (error: HttpErrorResponse) => {
          this.activateStore.setError(error);
        });
  }

  @action('resendToken')
  resendToken(token: string) {
    this.activateStore.setError(null);
    this.activateStore.setLoading(true);
    this.activateStore.setState('resending');

    this.http.post(`${this.CONSTANTS.apiPrefixV1}/resend-registration-token-by-token`, {}, {params: {token}})
      .pipe(delay(1000), finalize(() => this.activateStore.setLoading(false)))
      .subscribe(
        () => {
        },
        (error: HttpErrorResponse) => {
          this.activateStore.setError(error);
        }
      );
  }
}
