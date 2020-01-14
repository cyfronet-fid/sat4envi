import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {SessionQuery} from '../../state/session/session.query';

@Injectable({providedIn: 'root'})
export class IsLoggedIn implements CanActivate {
  constructor(protected sessionQuery: SessionQuery, protected router: Router) {}

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): UrlTree | boolean {
    if (!this.sessionQuery.isLoggedIn()) {
      return this.router.parseUrl('/login');
    }
    return true;
  }
}


@Injectable({providedIn: 'root'})
export class IsNotLoggedIn extends IsLoggedIn implements CanActivate {

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): UrlTree | boolean {
    if (this.sessionQuery.isLoggedIn()) {
      return this.router.parseUrl('/map/products');
    }
    return true
  }
}
