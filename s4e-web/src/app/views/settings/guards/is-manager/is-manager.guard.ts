import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable} from 'rxjs';
import {map, take} from 'rxjs/operators';
import {SessionQuery} from '../../../../state/session/session.query';

@Injectable({
  providedIn: 'root'
})
export class IsManagerGuard implements CanActivate {
  constructor(private sessionQuery: SessionQuery, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.sessionQuery.selectCanSeeInstitutions().pipe(take(1),
      map(manager => manager ? true : this.router.parseUrl('/settings/profile')));
  }
}
