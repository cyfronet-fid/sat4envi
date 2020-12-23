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

import { environment } from 'src/environments/environment';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {MapViewComponent} from './map-view.component';
import {MapModule} from './map.module';
import {RouterTestingModule} from '@angular/router/testing';
import {By} from '@angular/platform-browser';
import {MapService} from './state/map/map.service';
import {ActivatedRoute, ActivatedRouteSnapshot, UrlSegment} from '@angular/router';
import {take} from 'rxjs/operators';
import {MapStore} from './state/map/map.store';
import {SceneStore} from './state/scene/scene.store.service';
import {SceneFactory} from './state/scene/scene.factory.spec';
import {SessionStore} from '../../state/session/session.store';
import {LocalStorageTestingProvider, RemoteConfigurationTestingProvider} from '../../app.configuration.spec';
import waitForExpect from 'wait-for-expect';

describe('MapViewComponent', () => {
  let component: MapViewComponent;
  let sessionStore: SessionStore;
  let mapStore: MapStore;
  let fixture: ComponentFixture<MapViewComponent>;
  let route: ActivatedRoute;
  let sceneStore: SceneStore;

  function setActivatedChildPath(path: string) {
    route.snapshot = {
      children: [
        {
          url: [
            new UrlSegment(path, {})
          ]
        }
      ]
    } as ActivatedRouteSnapshot;
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider, RemoteConfigurationTestingProvider],
      imports: [MapModule, RouterTestingModule, HttpClientTestingModule]
    })
      .compileComponents();
    sessionStore = TestBed.get(SessionStore);
    mapStore = TestBed.get(MapStore);
    sceneStore = TestBed.get(SceneStore);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapViewComponent);
    component = fixture.componentInstance;
    route = TestBed.get(ActivatedRoute);
    setActivatedChildPath('products');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should isLinkActive return true if sub path in router is equal to argument', () => {
    expect(component.isLinkActive('sentinel-search')).toBeFalsy();
    setActivatedChildPath('sentinel-search');
    expect(component.isLinkActive('sentinel-search')).toBeTruthy();
  });

  it('activated route should relate to isLinkActive', () => {
    setActivatedChildPath('sentinel-search');
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(1):not(.active)'));
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(2).active'));
    setActivatedChildPath('products');
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(1).active'));
    fixture.debugElement.queryAll(By.css('.section.sidebar > ul.switch:nth-child(2):not(.active)'));
  });

  describe('ZK Options', () => {
    it('should not show #zk-options-button if user is not memberZK', () => {
      sessionStore.update({memberZK: false});
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css('#zk-options-button'))).toBeNull();
    });

    it('toggleZKOptions should call mapService.toggleZKOptions ', () => {
      const service: MapService = TestBed.get(MapService);
      const spy = spyOn(service, 'toggleZKOptions');
      component.toggleZKOptions();
      expect(spy).toHaveBeenCalledWith(true);
    });
  });

  it('sidebarOpen$ should trigger map update size', async () => {
    const spy = spyOn(component.mapComponent, 'updateSize');
    mapStore.update({sidebarOpen: true});
    await waitForExpect(() => { expect(spy).toHaveBeenCalled(); });
  });
});
