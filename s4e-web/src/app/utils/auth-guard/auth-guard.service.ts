import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {SessionQuery} from '../../state/session/session.query';
import {
  TOKEN_QUERY_PARAMETER,
  REJECTION_QUERY_PARAMETER,
  InvitationService
} from 'src/app/views/settings/people/state/invitation/invitation.service';

@Injectable({providedIn: 'root'})
export class IsLoggedIn implements CanActivate {
  constructor(
    protected _invitationService: InvitationService,
    protected _sessionQuery: SessionQuery,
    protected _router: Router
  ) {}

  canActivate(next: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): UrlTree | boolean {
    if (!this._sessionQuery.isLoggedIn()) {
      return this._router.parseUrl('/login');
    }
    return true;
  }
}


@Injectable({providedIn: 'root'})
export class IsNotLoggedIn extends IsLoggedIn implements CanActivate {

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): UrlTree | boolean {
    if (this._sessionQuery.isLoggedIn()) {
      if (next.queryParamMap.has(TOKEN_QUERY_PARAMETER)) {
        this._handleInvitation(next);
      }

      return this._router.parseUrl('/map/products');
    }

    return true;
  }

  protected _handleInvitation(next: ActivatedRouteSnapshot) {
    const token = next.queryParamMap.get(TOKEN_QUERY_PARAMETER);

    const isRejected = next.queryParamMap.has(REJECTION_QUERY_PARAMETER);
    if (isRejected) {
      this._invitationService.reject(token);
    }

    this._invitationService.confirm(token);
    return this._router.parseUrl('');
  }
}
