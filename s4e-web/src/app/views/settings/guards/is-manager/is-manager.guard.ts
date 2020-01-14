import { Injectable } from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree} from '@angular/router';
import { Observable } from 'rxjs';
import {ProfileQuery} from '../../../../state/profile/profile.query';
import {map, take} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class IsManagerGuard implements CanActivate {
  constructor(private profileQuery: ProfileQuery, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean|UrlTree> | Promise<boolean|UrlTree> | boolean|UrlTree {
    return this.profileQuery.selectCanSeeInstitutions().pipe(take(1),
      map(manager => manager ? true : this.router.parseUrl('/settings/profile')))
  }
}
