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

import {TestBed} from '@angular/core/testing';
import {NotificationMapperService} from './notification-mapper.service';
import {NotificationsModule} from '../notifications.module';
import {Component} from '@angular/core';
import {DefaultNotificationClazz, NotificationClazz} from '../notifications.tokens';
import {GeneralNotificationComponent} from '../components/general-notification/general-notification.component';

@Component({
  selector: 'mock-notification',
  template: '<div></div>',
  styleUrls: []
})
export class MockNotificationComponent {}

describe('NotificationMapperService', () => {
  let service: NotificationMapperService;

  describe('Standard providers', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [NotificationsModule.forRoot()],
        providers: [
          {
            provide: NotificationClazz,
            useValue: {
              name: 'MockNotificationComponent',
              component: MockNotificationComponent
            },
            multi: true
          }
        ]
      });
      service = TestBed.inject(NotificationMapperService);
    });

    it('clazzToComponent should work', () => {
      expect(service.clazzToComponent('MockNotificationComponent')).toBe(
        MockNotificationComponent
      );
    });

    it('clazzToComponent should give default class', () => {
      expect(service.clazzToComponent('NonExistentClass')).toBe(
        GeneralNotificationComponent
      );
    });
  });

  describe('Overwritten default notification', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [NotificationsModule.forRoot()],
        providers: [
          {provide: DefaultNotificationClazz, useValue: MockNotificationComponent}
        ]
      });
      service = TestBed.inject(NotificationMapperService);
    });

    it('clazzToComponent should give default class', () => {
      expect(service.clazzToComponent('NonExistentClass')).toBe(
        MockNotificationComponent
      );
    });
  });
});
