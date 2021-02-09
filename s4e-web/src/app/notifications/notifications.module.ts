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

import {ModuleWithProviders, Inject, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NotificationOutletComponent} from './components/notification-outlet/notification-outlet.component';
import {GeneralNotificationComponent} from './components/general-notification/general-notification.component';
import {NotificationQuery} from './state/notification.query';
import {NotificationService} from './state/notification.service';
import {DynamicNotificationComponent} from './components/dynamic-notification/dynamic-notification.component';
import {NotificationStore} from './state/notification.store';
import {
  DefaultNotificationClazz,
  NotificationAction,
  NotificationClazz,
  Production
} from './notifications.tokens';
import {devConnectNotifications} from './utils/utils';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

@NgModule({
  declarations: [
    NotificationOutletComponent,
    GeneralNotificationComponent,
    DynamicNotificationComponent
  ],
  imports: [CommonModule, BrowserAnimationsModule],
  exports: [NotificationOutletComponent],
  providers: [
    NotificationStore,
    NotificationQuery,
    NotificationService,
    {
      provide: NotificationAction,
      useFactory(notificationService: NotificationService) {
        return [
          'dismiss',
          notification => notificationService.remove(notification.id)
        ];
      },
      multi: true,
      deps: [NotificationService]
    },
    {
      provide: NotificationClazz,
      useValue: {
        name: 'GeneralNotificationComponent',
        component: GeneralNotificationComponent
      },
      multi: true
    },
    {provide: DefaultNotificationClazz, useValue: GeneralNotificationComponent}
  ],
  entryComponents: [GeneralNotificationComponent]
})
export class NotificationsModule {
  static forRoot(
    production: boolean = true
  ): ModuleWithProviders<NotificationsModule> {
    return {
      ngModule: NotificationsModule,
      providers: [{provide: Production, useValue: production}]
    };
  }

  constructor(
    notificationService: NotificationService,
    notificationQuery: NotificationQuery,
    @Inject(Production) production?: boolean
  ) {
    if (!production) {
      devConnectNotifications(notificationService, notificationQuery);
    }
  }
}
