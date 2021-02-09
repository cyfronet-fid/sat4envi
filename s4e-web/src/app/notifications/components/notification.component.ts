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
