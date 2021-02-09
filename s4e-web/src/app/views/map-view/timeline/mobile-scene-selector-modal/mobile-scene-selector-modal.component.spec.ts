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
import {MobileSceneSelectorModalComponent} from './mobile-scene-selector-modal.component';
import {MapModule} from '../../map.module';
import {
  LocalStorageTestingProvider,
  RemoteConfigurationTestingProvider
} from '../../../../app.configuration.spec';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('MobileSceneSelectorModalComponent', () => {
  let component: MobileSceneSelectorModalComponent;
  let fixture: ComponentFixture<MobileSceneSelectorModalComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [MapModule, RouterTestingModule, HttpClientTestingModule],
        providers: [RemoteConfigurationTestingProvider, LocalStorageTestingProvider]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(MobileSceneSelectorModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
