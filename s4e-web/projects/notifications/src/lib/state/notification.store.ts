import {Injectable} from '@angular/core';
import {EntityStore, StoreConfig} from '@datorama/akita';
import {Notification, NotificationState} from './notification.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Notification' })
export class NotificationStore extends EntityStore<NotificationState, Notification> {
  constructor() { super(); }
}

