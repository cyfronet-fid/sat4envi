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

import {NotificationService} from '../state/notification.service';
import {NotificationQuery} from '../state/notification.query';
import {devConnectNotifications} from './utils';
import {ID} from '@datorama/akita';
import {createGeneralNotification} from '../state/notification.model';
import {fakeAsync, tick} from '@angular/core/testing';

class MockQuery {
  getAll() {}
}

class MockService {
  remove(id: ID) {}
}

describe('devConnectNotifications', () => {
  let service: MockService;
  let query: MockQuery;

  beforeEach(() => {
    service = new MockService();
    query = new MockQuery();
  });

  it('should create delayed delete', fakeAsync(() => {
    const notifications = [
      createGeneralNotification({duration: 0}),
      createGeneralNotification({duration: 100})
    ];

    let spyRemove = spyOn(service, 'remove').and.stub();
    let spyGetAll = spyOn(query, 'getAll').and.returnValue(notifications);

    devConnectNotifications(
      service as NotificationService,
      query as NotificationQuery
    );

    tick(100);

    expect(spyRemove).toHaveBeenCalledTimes(1);
    expect(spyRemove).toHaveBeenCalledWith(notifications[1].id);
  }));
});
