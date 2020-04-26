import {Inject, Injectable, Type} from '@angular/core';
import {HashMap} from '@datorama/akita';
import {NotificationComponent} from '../components/notification.component';
import {Notification} from '../state/notification.model';
import {DefaultNotificationClazz, NotificationClazz} from '../notifications.tokens';
export type NotificationClazzMapping = HashMap<Type<NotificationComponent<Notification>>>

@Injectable({
  providedIn: 'root'
})
export class NotificationMapperService {
  constructor(@Inject(NotificationClazz) private notificationComponents: { name: string, component: Type<NotificationComponent<any>> }[],
              @Inject(DefaultNotificationClazz) private DEFAULT_COMPONENT: Type<NotificationComponent<any>>) {
    this.notificationComponents.forEach(componentDef => this._mapping[componentDef.name] = componentDef.component);
  }

  private _mapping: NotificationClazzMapping = {};

  get mapping(): HashMap<Type<any>> {
    return this._mapping;
  }

  clazzToComponent(clazz: string): Type<NotificationComponent<Notification>> {
    return this.mapping[clazz] || this.DEFAULT_COMPONENT;
  }
}
