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

import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NotificationService} from './notification.service';
import {NotificationStore} from './notification.store';
import {NotificationsModule} from '../notifications.module';
import {NotificationQuery} from './notification.query';
import {createGeneralNotification, createNotification} from './notification.model';

describe('NotificationService', () => {
  let service: NotificationService;
  let store: NotificationStore;
  let query: NotificationQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NotificationsModule.forRoot()]
    });

    query = TestBed.inject(NotificationQuery);
    service = TestBed.inject(NotificationService);
    store = TestBed.inject(NotificationStore);
  });

  it('add', fakeAsync(() => {
    const notification = createNotification({duration: 1});
    service.add(notification);
    tick(1000);
    expect(query.getAll().length).toBe(0);
  }));

  it('remove', () => {
    const notification = createNotification();
    store.add(notification);
    service.remove(notification.id);
    expect(query.getAll().length).toBe(0);
  });

  it('addGeneral', () => {
    spyOn(service, 'add');
    service.addGeneral({id: '1234'});
    expect(service.add).toHaveBeenCalledWith(
      createGeneralNotification({id: '1234'})
    );
  });
});
