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
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import {ConfigurationService} from './configuration.service';
import {ConfigurationStore} from './configuration.store';
import {ConfigurationQuery} from './configuration.query';
import {take, toArray, catchError} from 'rxjs/operators';
import {MapModule} from '../../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import environment from 'src/environments/environment';
import {of} from 'rxjs';
import {NotificationService} from '../../../../../notifications/state/notification.service';

describe('ConfigurationService', () => {
  let service: ConfigurationService;
  let query: ConfigurationQuery;
  let store: ConfigurationStore;
  let http: HttpTestingController;
  let notifications: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    notifications = TestBed.inject(NotificationService);
    query = TestBed.inject(ConfigurationQuery);
    service = TestBed.inject(ConfigurationService);
    store = TestBed.inject(ConfigurationStore);
    http = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('shareConfiguration', () => {
    const body = {
      thumbnail: '00',
      path: '/abc?a=1',
      emails: ['a@a'],
      description: 'test',
      caption: 'test'
    };

    it('should call http, pass data and return true if ok', async () => {
      spyOn(notifications, 'addGeneral').and.stub();
      service
        .shareConfiguration(body)
        .subscribe(value => expect(value).toBeTruthy());
      const req = http.expectOne({
        method: 'POST',
        url: `${environment.apiPrefixV1}/share-link`
      });
      req.flush({});

      expect(req.request.body).toEqual(body);

      const notification = {
        content: 'Link został wysłany na adres a@a',
        type: 'success'
      };
      expect(notifications.addGeneral).toHaveBeenCalledWith(notification);
    });

    it('should return set store error and return false if http errored', async () => {
      const error = service
        .shareConfiguration(body)
        .pipe(catchError(error => of(error)))
        .toPromise();
      const url = `${environment.apiPrefixV1}/share-link`;
      const req = http.expectOne({method: 'POST', url});
      req.flush({}, {status: 500, statusText: 'server error'});

      expect(req.request.body).toEqual(body);
      expect((await error).status).toEqual(500);
    });

    it('should set loading', done => {
      query
        .selectLoading()
        .pipe(take(3), toArray())
        .subscribe(data => {
          expect(data).toEqual([false, true, false]);
          done();
        });
      service.shareConfiguration(body).subscribe();

      const url = `${environment.apiPrefixV1}/share-link`;
      const r = http.expectOne(url);
      r.flush({content: []});
    });
  });
});
