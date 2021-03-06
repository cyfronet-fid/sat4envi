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

import {SaveConfigModalComponent} from './save-config-modal.component';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {SAVE_CONFIG_MODAL_ID} from './save-config-modal.model';
import {ViewConfigurationService} from '../../state/view-configuration/view-configuration.service';
import {ViewConfigurationQuery} from '../../state/view-configuration/view-configuration.query';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {MapModule} from '../../map.module';
import {By} from '@angular/platform-browser';
import {
  ViewConfiguration,
  ViewConfigurationEx
} from '../../state/view-configuration/view-configuration.model';
import {of} from 'rxjs';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';

describe('SaveConfigModalComponent', () => {
  let component: SaveConfigModalComponent;
  let fixture: ComponentFixture<SaveConfigModalComponent>;
  let service: ViewConfigurationService;
  const viewConf: ViewConfigurationEx = {
    caption: '',
    configuration: {
      overlays: [1, 2],
      productId: 51,
      sceneId: 101,
      date: '11-11-2020',
      viewPosition: {
        zoomLevel: 10,
        centerCoordinates: [56, 67]
      },
      manualDate: null
    },
    configurationNames: {
      overlays: ['overlay 1', 'overlay 3'],
      product: 'product 1',
      selectedDate: '11-11-2020'
    },
    thumbnail: 'data:image/png;base64,00'
  };

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [MapModule, HttpClientTestingModule, RouterTestingModule],
        providers: [
          LocalStorageTestingProvider,
          {
            provide: MODAL_DEF,
            useValue: {
              id: SAVE_CONFIG_MODAL_ID,
              size: 'lg',
              viewConfiguration: viewConf
            }
          }
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(SaveConfigModalComponent);
    component = fixture.componentInstance;
    service = TestBed.inject(ViewConfigurationService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('submitting form should call accept', () => {
    const spy = spyOn(component, 'accept').and.stub();
    fixture.debugElement
      .query(By.css('button[type="submit"]'))
      .nativeElement.click();
    expect(spy).toHaveBeenCalled();
  });

  it('configuration name should be initially taken from selected date', () => {
    const spy = jest.spyOn(service, 'add$');
    spy.mockReturnValueOnce(of(true));

    const expected: ViewConfiguration = {
      ...viewConf,
      thumbnail: '00',
      caption: ''
    };

    fixture.debugElement
      .query(By.css('button[type="submit"]'))
      .nativeElement.click();
    expect(spy).not.toHaveBeenCalledWith(expected);
  });

  it('accepting form should call service', () => {
    const spy = jest.spyOn(service, 'add$');
    spy.mockReturnValueOnce(of(true));

    const configuratioNameInput = component.form.controls.configurationName;
    configuratioNameInput.setValue('new name');

    const expected: ViewConfiguration = {
      ...viewConf,
      thumbnail: '00',
      caption: 'new name'
    };

    fixture.debugElement
      .query(By.css('button[type="submit"]'))
      .nativeElement.click();
    expect(spy).toHaveBeenCalledWith(expected);
  });

  it('accepting form should close modal', async () => {
    component.form.controls.configurationName.setValue('my scene config');
    spyOn(service, 'add$').and.returnValue(of(true));
    const dimissSpy = jest.spyOn(component, 'dismiss');

    await component.accept();
    expect(dimissSpy).toHaveBeenCalled();
  });
});
