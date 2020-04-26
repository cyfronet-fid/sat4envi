import {Injectable} from '@angular/core';
import {ID} from '@datorama/akita';
import {NotificationStore} from './notification.store';
import {createGeneralNotification, GeneralNotification, Notification} from './notification.model';
import {delay} from 'rxjs/operators';
import {NotificationQuery} from './notification.query';
import {of} from 'rxjs';

@Injectable({providedIn: 'root'})
export class NotificationService {
  constructor(private notificationStore: NotificationStore,
              private notificationQuery: NotificationQuery) {
  }

  add(notification: Notification) {
    this.notificationStore.add(notification);
    if (notification.duration > 0) {
      of(notification.id).pipe(delay(notification.duration)).subscribe(id => {
        if (this.notificationQuery.hasEntity(id)) {
          this.remove(id);
        }
      });
    }
  }

  remove(id: ID) {
    this.notificationStore.remove(id);
  }

  addGeneral(notification: Partial<GeneralNotification>) {
    this.add(createGeneralNotification(notification));
  }
}
