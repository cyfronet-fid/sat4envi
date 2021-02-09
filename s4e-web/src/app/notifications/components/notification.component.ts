import {EventEmitter, Input, OnInit, Output, Directive} from '@angular/core';
import {Notification} from '../state/notification.model';
import {ID} from '@datorama/akita';

@Directive()
export class NotificationComponent<NotificationClass extends Notification>
  implements OnInit {
  @Input() notification: NotificationClass;
  @Output() activated: EventEmitter<ID> = new EventEmitter<ID>();

  ngOnInit() {
    if (!this.notification) {
      throw Error(
        'notification-component inheriting NotificationComponent must set [notification]'
      );
    }
  }
}
