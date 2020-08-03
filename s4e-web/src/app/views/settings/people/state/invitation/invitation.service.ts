import { InjectorModule } from './../../../../../common/injector.module';
import { InvitationStore } from './invitation.store';
import { Institution } from '../../../state/institution/institution.model';
import { NotificationService } from '../../../../../../../projects/notifications/src/lib/state/notification.service';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, ActivatedRoute } from '@angular/router';
import { httpGetRequest$, httpPutRequest$ } from 'src/app/common/store.util';
import { Invitation } from './invitation.model';

export const TOKEN_QUERY_PARAMETER = 'token';
export const REJECTION_QUERY_PARAMETER = 'reject';

@Injectable({
  providedIn: 'root'
})
export class InvitationService {
  constructor(
    private _http: HttpClient,
    private _notificationService: NotificationService,
    private _invitationStore: InvitationStore
  ) {}

  public getBy(institution: Institution): void {
    const url = `/institutions/${institution.slug}/invitations`;
    httpGetRequest$<Invitation[]>(this._http, url, this._invitationStore)
      .subscribe(invitations => this._invitationStore.set(invitations));
  }

  public resend(invitation: Invitation, institution: Institution) {
    const notificationMessage = 'Zaproszenie zostało ponownie wysłane';
    const url = `/institutions/${institution.slug}/invitation/${invitation.token}`;
    httpPutRequest$<Invitation>(this._http, url, {email: invitation.email} as any, this._invitationStore)
      .subscribe((newInvitation) => {
        this._invitationStore.remove(invitation.email);
        this._invitationStore.add(newInvitation);
        this._notificationService.addGeneral({
          content: notificationMessage,
          type: 'success'
        });
      });
  }

  public create(institutionSlug: string, email: string): void {
    const notificationMessage = 'Zaproszenie zostało wysłane';
    const url = `/api/v1/institutions/${institutionSlug}/invitations`;
    this._http.post<Invitation>(url, {email})
      .subscribe((newInvitation) => {
        this._invitationStore.add(newInvitation);
        this._notificationService.addGeneral({
          content: notificationMessage,
          type: 'success'
        });
      });
  }

  public confirm(token: string): void {
    const notificationMessage = 'Zostałeś dodany do instytucji';
    const url = `/api/v1/invitations/${token}/confirm`;
    this._http.post<Institution>(url, {})
      .subscribe((institution: Institution) => {
        // IMPORTANT!!!
        // Router is injected, because injection in the APP_INITIALIZER creates circular-dependency
        InjectorModule.Injector.get(Router)
          .navigate(
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
    const notificationMessage = 'Twoje zaproszenie zostało odrzucone';
    const url = `/api/v1/invitations/${token}/reject`;
    this._http.put(url, {})
      .subscribe(() => this._notificationService.addGeneral({
        content: notificationMessage,
        type: 'success'
      }));
  }
}
