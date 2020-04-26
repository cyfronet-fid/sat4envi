import {Component, Inject, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {NotificationQuery} from '../../state/notification.query';
import {Notification} from '../../state/notification.model';
import {ID} from '@datorama/akita';
import {NotificationService} from '../../state/notification.service';
import {NotificationAction} from '../../notifications.tokens';
import {animate, style, transition, trigger} from '@angular/animations';

@Component({
  selector: 'notification-outlet',
  templateUrl: './notification-outlet.component.html',
  styleUrls: ['./notification-outlet.component.scss'],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: '0' }),
        animate('.2s ease-out', style({ opacity: '1' })),
      ]),
      transition(':leave', [
        style({ opacity: '1' }),
        animate('.2s ease-out', style({ opacity: '0' })),
      ]),
    ])
  ],
})
export class NotificationOutletComponent implements OnInit {
  public notifications$: Observable<Notification[]>;
  public trackNotification = (index: number, item: Notification) => item.id;

  constructor(private notificationQuery: NotificationQuery,
              @Inject(NotificationAction) private notificationActions: any[],
              private notificationService: NotificationService) { }

  ngOnInit() {
    this.notifications$ = this.notificationQuery.selectAll();
  }

  activated(id: ID, ctx?: string) {
    const [actionId, action] = this.notificationActions.find(([_actionId, _action]: [string, (Notification) => {}]) => _actionId === this.notificationQuery.getEntity(id).action) || [undefined, undefined];

    if (actionId === undefined) {
      throw new Error(`${this.notificationQuery.getEntity(id).action} has not been found - did you register it in the module\'s providers?`)
    }

    action(this.notificationQuery.getEntity(id));
  }
}
