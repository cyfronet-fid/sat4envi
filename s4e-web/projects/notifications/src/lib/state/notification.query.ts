import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {NotificationStore} from './notification.store';
import {Notification, NotificationState} from './notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationQuery extends QueryEntity<NotificationState, Notification> {
  constructor(protected store: NotificationStore) { super(store); }
}
