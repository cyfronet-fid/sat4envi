import { Institution } from './../../state/institution/institution.model';
import { NotificationService } from './../../../../../../projects/notifications/src/lib/state/notification.service';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ParamMap, Route, Router, ActivatedRoute, ActivatedRouteSnapshot } from '@angular/router';
import { SessionQuery } from 'src/app/state/session/session.query';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export const TOKEN_QUERY_PARAMETER = 'token';
export const REJECTION_QUERY_PARAMETER = 'reject';

@Injectable({
  providedIn: 'root'
})
export class InvitationService {
  constructor(
    private _http: HttpClient,
    private _notificationService: NotificationService,
    private _sessionQuery: SessionQuery,
    private _router: Router,
    private _activatedRoute: ActivatedRoute
  ) {}

  public get(): void {}

  public create(institutionSlug: string, email: string): void {
    const notificationMessage = 'Zaproszenie zostało wysłane';
    const url = `/api/v1/institutions/${institutionSlug}/invitations`;
    this._http.post(url, {email})
      .subscribe(() => this._notificationService.addGeneral({
        content: notificationMessage,
        type: 'success'
      }));
  }

  public accept(token: string): void {
    const notificationMessage = 'Zostałeś dodany do instytucji';
    const url = `/api/v1/invitations/${token}/confirm`;
    this._http.post<Institution>(url, {})
      .subscribe((institution: Institution) => {
        this._router.navigate(
          ['/settings/institution'],
          {
            queryParams: {
              institution: institution.slug
            }
          }
        )
        .then(() => this._notificationService.addGeneral({
          content: notificationMessage,
          type: 'success'
        }));
      });
  }

  public reject(token: string): void {
    const notificationMessage = 'Twoje zaproszenie zostało poprawnie odrzucone';
    const url = `/api/v1/invitations/${token}/reject`;
    this._http.put(url, {})
      .subscribe(() => {
        this._notificationService.addGeneral({
          content: notificationMessage,
          type: 'success'
        });
        this._router.navigate(['.']);
      });
  }

  public handleInvitation(next: ActivatedRouteSnapshot) {
    const token = next.queryParamMap.get(TOKEN_QUERY_PARAMETER);

    const isRejected = next.queryParamMap.has(REJECTION_QUERY_PARAMETER);
    if (isRejected) {
      this.reject(token);
    }

    this.accept(token);
  }
}
