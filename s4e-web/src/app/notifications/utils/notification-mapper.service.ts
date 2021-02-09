/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {Inject, Injectable, Type} from '@angular/core';
import {HashMap} from '@datorama/akita';
import {NotificationComponent} from '../components/notification.component';
import {Notification} from '../state/notification.model';
import {DefaultNotificationClazz, NotificationClazz} from '../notifications.tokens';
export type NotificationClazzMapping = HashMap<
  Type<NotificationComponent<Notification>>
>;

@Injectable({
  providedIn: 'root'
})
export class NotificationMapperService {
  constructor(
    @Inject(NotificationClazz)
    private notificationComponents: {
      name: string;
      component: Type<NotificationComponent<any>>;
    }[],
    @Inject(DefaultNotificationClazz)
    private DEFAULT_COMPONENT: Type<NotificationComponent<any>>
  ) {
    this.notificationComponents.forEach(
      componentDef => (this._mapping[componentDef.name] = componentDef.component)
    );
  }

  private _mapping: NotificationClazzMapping = {};

  get mapping(): HashMap<Type<any>> {
    return this._mapping;
  }

  clazzToComponent(clazz: string): Type<NotificationComponent<Notification>> {
    return this.mapping[clazz] || this.DEFAULT_COMPONENT;
  }
}
