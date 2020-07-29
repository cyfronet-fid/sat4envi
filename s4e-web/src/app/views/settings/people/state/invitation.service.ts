import { NotificationService } from './../../../../../../projects/notifications/src/lib/state/notification.service';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class InvitationService {
  constructor(
    private _http: HttpClient,
    private _notificationService: NotificationService
  ) {}

  get(): void {}
  create(institutionSlug: string, email: string): void {
    const url = `/institutions/${institutionSlug}/invitations`;
    this._http.post(url, {email}).subscribe();
  }
  accept(): void {}
  reject(): void {}
}
