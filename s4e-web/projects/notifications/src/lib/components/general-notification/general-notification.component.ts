import {Component} from '@angular/core';
import {GeneralNotification} from '../../state/notification.model';
import {NotificationComponent} from '../notification.component';

@Component({
  selector: 'general-notification',
  templateUrl: './general-notification.component.html',
  styleUrls: ['./general-notification.component.scss']
})
export class GeneralNotificationComponent extends NotificationComponent<GeneralNotification> {}
