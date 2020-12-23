/*
 * Copyright 2020 ACC Cyfronet AGH
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
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {ViewConfigurationService} from './view-configuration.service';
import {ViewConfigurationStore} from './view-configuration.store';
import {take, toArray} from 'rxjs/operators';
import {ViewConfigurationQuery} from './view-configuration.query';
import {ViewConfiguration} from './view-configuration.model';
import {MapModule} from '../../map.module';
import {RouterTestingModule} from '@angular/router/testing';
import environment from 'src/environments/environment';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';

describe('ViewConfigurationService', () => {
  let service: ViewConfigurationService;
  let store: ViewConfigurationStore;
  let query: ViewConfigurationQuery;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [MapModule, HttpClientTestingModule, RouterTestingModule]
    });

    service = TestBed.get(ViewConfigurationService);
    store = TestBed.get(ViewConfigurationStore);
    query = TestBed.get(ViewConfigurationQuery);
    http = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  describe('get', () => {
    it('loading should be set', (done) => {
      const stream = query.selectLoading();

      stream.pipe(take(3), toArray()).subscribe(data => {
        expect(data).toEqual([false, true, false]);
        done();
      });

      service.get();

      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views`);
      r.flush({content: []});
    });

    it('should call http endpoint', () => {
      service.get();
      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views`);
      r.flush({content: []});
      expect(r.request.method).toBe('GET');
    });

    it('should set state in store', () => {
      service.get();

      const sampleView: ViewConfiguration = {
        caption: 'test',
        configuration: {
          overlays: ['ov'], productId: 123, sceneId: 3, date: '03-03-2021', viewPosition: {
            centerCoordinates: [1, 2],
            zoomLevel: 4
          }
        },
        thumbnail: 'base64string'
      };

      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views`);
      r.flush({content: [sampleView]});
      expect(query.getAll()).toEqual([sampleView]);
    });
  });

  describe('delete', () => {
    it('loading should be set', (done) => {
      const stream = query.selectLoading();

      stream.pipe(take(3), toArray()).subscribe(data => {
        expect(data).toEqual([false, true, false]);
        done();
      });

      service.delete('uuid');

      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views/uuid`);
      r.flush({});
    });

    it('should call http endpoint', () => {
      service.delete('uuid');
      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views/uuid`);
      r.flush({});
      expect(r.request.method).toBe('DELETE');
    });

    it('should remove from store', () => {
      service.delete('uuid');
      let storeSpy = jest.spyOn(store, 'remove');
      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views/uuid`);
      r.flush({});
      expect(storeSpy).toHaveBeenCalledWith('uuid');
    });
  });

  describe('add$', () => {
    it('loading should be set', (done) => {
      const stream = query.selectLoading();

      stream.pipe(take(3), toArray()).subscribe(data => {
        expect(data).toEqual([false, true, false]);
        done();
      });

      service.add$({
        caption: '',
        configuration: {overlays: [], productId: undefined, sceneId: undefined, date: '', viewPosition: undefined},
        thumbnail: ''
      }).subscribe();

      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views`);
      r.flush({});
    });

    it('should call http endpoint', () => {
      const viewConf: ViewConfiguration = {
        caption: 'test caption',
        configuration: {
          overlays: ['123'], productId: 3, sceneId: 76, date: '12-12-2012', viewPosition: undefined
        },
        thumbnail: ''
      };
      service.add$(viewConf).subscribe();
      const r = http.expectOne(`${environment.apiPrefixV1}/saved-views`);
      r.flush({});
      expect(r.request.method).toBe('POST');
      expect(r.request.body).toBe(viewConf);
    });
  });

});
