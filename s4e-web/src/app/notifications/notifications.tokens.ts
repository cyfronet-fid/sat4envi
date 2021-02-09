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
