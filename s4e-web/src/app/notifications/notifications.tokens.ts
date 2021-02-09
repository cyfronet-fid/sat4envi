import {InjectionToken, Type} from '@angular/core';
import {NotificationComponent} from './components/notification.component';
import {Notification} from './state/notification.model';

export const NotificationClazz = new InjectionToken<{
  name: string;
  component: Type<NotificationComponent<any>>;
}>('NotificationClazz');
export const NotificationAction = new InjectionToken<
  [string, (n: Notification) => {}]
>('NotificationAction');
export const DefaultNotificationClazz = new InjectionToken<
  Type<NotificationComponent<any>>
>('DefaultNotificationClazz');
export const Production = new InjectionToken<boolean>('Production');
