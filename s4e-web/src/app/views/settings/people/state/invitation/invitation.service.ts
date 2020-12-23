/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {environment} from '../../../../../../environments/environment';
import {InvitationStore} from './invitation.store';
import {Institution} from '../../../state/institution/institution.model';
import {NotificationService} from 'notifications';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivatedRouteSnapshot, Router} from '@angular/router';
import {finalize, switchMap, tap} from 'rxjs/operators';
import {Invitation, InvitationResendRequest} from './invitation.model';
import {handleHttpRequest$} from 'src/app/common/store.util';
import {Observable} from 'rxjs';
import {SessionService} from '../../../../../state/session/session.service';

export const TOKEN_QUERY_PARAMETER = 'token';
export const REJECTION_QUERY_PARAMETER = 'reject';

@Injectable({
  providedIn: 'root'
})
export class InvitationService {
  constructor(
    private _http: HttpClient,
    private _notificationService: NotificationService,
    private _router: Router,
    private _store: InvitationStore,
    private _sessionService: SessionService
  ) {
  }

  public getBy(institution: Institution): void {
    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/invitations`;
    this._http.get<Invitation[]>(url)
      .pipe(handleHttpRequest$(this._store))
      .subscribe((data) => this._store.set(data));
  }

  public resend(request: InvitationResendRequest, institution: Institution) {
    const notificationMessage = 'Zaproszenie zostało ponownie wysłane';
    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/invitations`;
    this._http.put<Invitation>(url, request)
      .pipe(
        handleHttpRequest$(this._store),
        tap((newInvitation) => {
          this._store.remove(request.oldEmail);
          this._store.add(newInvitation);
        }),
        finalize(() => this._notificationService.addGeneral({
          content: notificationMessage,
          type: 'success'
        }))
      )
      .subscribe();
  }

  public delete(invitation: Invitation, institution: Institution) {
    const notificationMessage = 'Zaproszenie zostało usunięte';
    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/invitations/${invitation.id}`;
    this._http.delete(url)
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._store.remove(invitation.email)),
        tap(() => this._notificationService.addGeneral({
          content: notificationMessage,
          type: 'success'
        }))
      )
      .subscribe();
  }

  public send(institutionSlug: string, email: string, forAdmin = false): Observable<Invitation> {
    const notificationMessage = 'Zaproszenie zostało wysłane';
    const url = `${environment.apiPrefixV1}/institutions/${institutionSlug}/invitations`;
    return this._http.post<Invitation>(url, {email, forAdmin})
      .pipe(
        handleHttpRequest$(this._store),
        tap((invitation: Invitation) => {
          this._store.add(invitation);
          this._notificationService.addGeneral({
            content: notificationMessage,
            type: 'success'
          });
        })
      );
  }

  public confirm(token: string): void {
    const notificationMessage = 'Zostałeś dodany do instytucji';
    const url = `${environment.apiPrefixV1}/invitations/${token}/confirm`;
    this._http.post<Institution>(url, {})
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._notificationService.addGeneral({
          content: notificationMessage,
          type: 'success'
        })),
        switchMap(() => this._sessionService.loadProfile$())
      )
      .subscribe();
  }

  public reject(token: string): void {
    const notificationMessage = 'Twoje zaproszenie zostało poprawnie odrzucone';
    const url = `${environment.apiPrefixV1}/invitations/${token}/reject`;
    this._http.put(url, {})
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => {
          this._notificationService.addGeneral({
            content: notificationMessage,
            type: 'success'
          });
          this._router.navigateByUrl('/map/products');
        })
      )
      .subscribe();
  }

  public handleInvitation(next: ActivatedRouteSnapshot) {
    const token = next.queryParamMap.get(TOKEN_QUERY_PARAMETER);
    const isRejected = next.queryParamMap.has(REJECTION_QUERY_PARAMETER);
    if (isRejected) {
      this.reject(token);
    }

    this.confirm(token);
  }
}
