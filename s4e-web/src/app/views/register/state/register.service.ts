import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RegisterStore } from './register.store';
import {of} from 'rxjs';
import {delay, finalize} from 'rxjs/operators';
import {Router} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class RegisterService {

  constructor(private registerStore: RegisterStore,
              private router: Router,
              private http: HttpClient) {
  }

  /**
   * :TODO - implement this
   * @param login
   * @param password
   */
  register(login: string, password: string) {
    this.registerStore.setLoading(true);
    of(true).pipe(delay(1000)).subscribe(data => {
      this.registerStore.setLoading(false);
      this.router.navigate(['/']);
    }, error => {
      this.registerStore.setError(error);
    });
  }
}
