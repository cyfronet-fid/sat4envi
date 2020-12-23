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

import { GLOBAL_OWNER_TYPE } from './overlay.model';
import {OverlayQuery} from './overlay.query';
import {OverlayStore} from './overlay.store';
import {async, TestBed} from '@angular/core/testing';
import {MapModule} from '../../map.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {RemoteConfigurationTestingProvider} from 'src/app/app.configuration.spec';
import {OverlayFactory} from './overlay.factory.spec';
import { InjectorModule } from 'src/app/common/injector.module';

describe('OverlayQuery', () => {
  let query: OverlayQuery;
  let store: OverlayStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MapModule,
        HttpClientTestingModule,
        RouterTestingModule,
        InjectorModule
      ],
      providers: [RemoteConfigurationTestingProvider]
    });

    query = TestBed.get(OverlayQuery);
    store = TestBed.get(OverlayStore);
  }));

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  describe('selectVisibleAsUIOverlays', () => {
    it('should work', (done) => {
      const overlay = OverlayFactory.build({
        label: 'test',
        ownerType: GLOBAL_OWNER_TYPE,
        id: 'test-overlay-id'
      });
      store.add(overlay);
      query.selectVisibleAsUIOverlays().subscribe(overlays => {

        expect(overlays.length).toEqual(1);

        expect((overlays[0].olLayer as any).values_.source.urls[0])
          .toEqual(overlay.url);

        expect(overlays.map(o => ({...o, olLayer: undefined}))).toEqual([
          {
            id: 'test-overlay-id',
            label: 'test',
            ownerType: GLOBAL_OWNER_TYPE,
            cid: 'test-overlay-id',
            active: false,
            favourite: false,
            isFavouriteLoading: false,
            isLoading: false,
            visible: true,
            url: overlay.url,
            createdAt: null,
            olLayer: undefined
          }
        ]);
        done();
      });
    });
  });
});
