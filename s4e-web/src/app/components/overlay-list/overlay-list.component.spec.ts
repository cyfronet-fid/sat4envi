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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {DebugElement} from '@angular/core';
import {RemoteConfigurationTestingProvider} from '../../app.configuration.spec';
import {MapModule} from '../../views/map-view/map.module';
import {OverlayService} from '../../views/map-view/state/overlay/overlay.service';
import {OverlayListComponent} from './overlay-list.component';
import {ReplaySubject, Subject} from 'rxjs';
import {ActivatedRoute, convertToParamMap, Data, ParamMap} from '@angular/router';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap> = new ReplaySubject(1);
  data: Subject<Data> = new ReplaySubject(1);

  constructor() {
    this.queryParamMap.next(convertToParamMap({institution: '1'}));
    this.data.next({isEditMode: false});
  }
}

describe('OverlayListModalComponent', () => {
  let component: OverlayListComponent;
  let fixture: ComponentFixture<OverlayListComponent>;
  let de: DebugElement;
  let overlayService: OverlayService;
  let activatedRoute: ActivatedRouteStub;

  beforeEach(
    waitForAsync(() => {
      activatedRoute = new ActivatedRouteStub();
      TestBed.configureTestingModule({
        imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
        providers: [
          RemoteConfigurationTestingProvider,
          {provide: ActivatedRoute, useValue: activatedRoute}
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(OverlayListComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    overlayService = TestBed.inject(OverlayService);
  });

  it('should create', () => {
    component.newOwner = 'PERSONAL';
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
  it('should valid url params on add', async () => {
    component.newOwner = 'PERSONAL';
    const spy = spyOn(overlayService, 'createPersonalOverlay$');

    const url = [
      'https://test-page.pl/?SERVICE=unknown',
      'REQUEST=unknown',
      'LAYERS=unkn?own',
      'STYLES=unkn?own',
      'FORMAT=image/unknown',
      'TRANSPARENT=none',
      'VERSION=x.y.z',
      'HEIGHT=none',
      'WIDTH=none',
      'CRS=unknown',
      'BBOX=unknown,unknown,unknown,unknown'
    ].join('&');
    const newLayer = {
      label: 'test',
      url
    };
    component.newOverlayForm.setValue(newLayer);

    await component.addNewOverlay();
    expect(spy).not.toHaveBeenCalled();
  });
});
