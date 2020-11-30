import {Injectable} from '@angular/core';
import {map, take} from 'rxjs/operators';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {SessionQuery} from '../../../state/session/session.query';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IsAdminGuard implements CanActivate {
  constructor(private sessionQuery: SessionQuery, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.sessionQuery.select('admin').pipe(take(1),
      map(isAdmin => isAdmin ? true : this.router.parseUrl('/settings')));
  }
}
