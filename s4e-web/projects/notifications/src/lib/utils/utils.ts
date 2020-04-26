import {NotificationService} from '../state/notification.service';
import {NotificationQuery} from '../state/notification.query';
import {of} from 'rxjs';
import {delay} from 'rxjs/operators';

export function devConnectNotifications(notificationService: NotificationService,
                                        notificationQuery: NotificationQuery) {
  notificationQuery.getAll()
    .filter(notification => notification.duration > 0)
    .forEach(notification => of(notification.id)
      .pipe(delay(notification.duration)).subscribe(id => notificationService.remove(id)));
}
