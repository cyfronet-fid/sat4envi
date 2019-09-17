import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {RegisterStore} from './register.store';
import {delay, finalize} from 'rxjs/operators';
import {Router} from '@angular/router';
import {S4eConfig} from '../../../utils/initializer/config.service';

@Injectable({providedIn: 'root'})
export class RegisterService {

  constructor(private registerStore: RegisterStore,
              private router: Router,
              private http: HttpClient,
              private CONFIG: S4eConfig) {
  }

  /**
   * @param email
   * @param password
   * @param recaptcha
   */
  register(email: string, password: string, recaptcha: string) {
    this.registerStore.setLoading(true);
    this.http.post(`${this.CONFIG.apiPrefixV1}/register`, {email, password}, {params: {'g-recaptcha-response': recaptcha}})
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
