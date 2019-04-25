import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {SessionQuery} from '../../state/session/session.query';

@Injectable({providedIn: 'root'})
export class IsLoggedIn implements CanActivate {

  constructor(protected sessionQuery: SessionQuery, protected router: Router) {
  }

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const isLoggedIn: boolean = this.sessionQuery.isLoggedIn();

    if (!isLoggedIn) {
      this.router.navigate(['/login']);
    }

    return isLoggedIn;
  }
}


@Injectable({providedIn: 'root'})
export class IsNotLoggedIn extends IsLoggedIn implements CanActivate {

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {

    const isLoggedIn: boolean = this.sessionQuery.isLoggedIn();

    if (isLoggedIn) {
      this.router.navigate(['/']);
    }

    return !isLoggedIn;
  }
}
