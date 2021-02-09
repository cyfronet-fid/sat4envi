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

import {Injectable} from '@angular/core';
import {ID} from '@datorama/akita';
import {NotificationStore} from './notification.store';
import {
  createGeneralNotification,
  GeneralNotification,
  Notification
} from './notification.model';
import {delay} from 'rxjs/operators';
import {NotificationQuery} from './notification.query';
import {of} from 'rxjs';

@Injectable({providedIn: 'root'})
export class NotificationService {
  constructor(
    private notificationStore: NotificationStore,
    private notificationQuery: NotificationQuery
  ) {}

  add(notification: Notification) {
    this.notificationStore.add(notification);
    if (notification.duration > 0) {
      of(notification.id)
        .pipe(delay(notification.duration))
        .subscribe(id => {
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
