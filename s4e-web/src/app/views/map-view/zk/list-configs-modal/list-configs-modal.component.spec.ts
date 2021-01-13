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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {ViewConfigurationService} from '../../state/view-configuration/view-configuration.service';
import {ViewConfigurationQuery} from '../../state/view-configuration/view-configuration.query';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {MapModule} from '../../map.module';
import {ListConfigsModalComponent} from './list-configs-modal.component';
import {LIST_CONFIGS_MODAL_ID} from './list-configs-modal.model';
import {ModalService} from '../../../../modal/state/modal.service';
import {ViewConfiguration, ViewRouterConfig} from '../../state/view-configuration/view-configuration.model';
import {MapService} from '../../state/map/map.service';
import {RouterTestingModule} from '@angular/router/testing';
import {LocalStorageTestingProvider} from '../../../../app.configuration.spec';
import {of} from 'rxjs';

describe('ListConfigsModalComponent', () => {
  let component: ListConfigsModalComponent;
  let fixture: ComponentFixture<ListConfigsModalComponent>;
  let service: ViewConfigurationService;
  let query: ViewConfigurationQuery;
  let modalService: ModalService;
  let mapService: MapService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MapModule, RouterTestingModule],
      providers: [
        LocalStorageTestingProvider,
        {
          provide: MODAL_DEF, useValue: {
            id: LIST_CONFIGS_MODAL_ID,
            size: 'lg',
          }
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListConfigsModalComponent);
    component = fixture.componentInstance;
    service = TestBed.get(ViewConfigurationService);
    query = TestBed.get(ViewConfigurationQuery);
    modalService = TestBed.get(ModalService);
    mapService = TestBed.get(MapService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('delete with confirmation should call service', async () => {
    spyOn(modalService, 'confirm').and.returnValue(Promise.resolve(true));
    const serviceSpy = jest.spyOn(service, 'delete');

    await component.deleteConfig({
        caption: '',
        configuration: {overlays: [], productId: undefined, sceneId: undefined, date: '', viewPosition: undefined},
        thumbnail: '',
        uuid: 'uuid1'
      }
    );
    expect(serviceSpy).toHaveBeenCalledWith('uuid1');
  });

  it('delete without confirmation should not call service', async () => {
    spyOn(modalService, 'confirm').and.returnValue(Promise.resolve(false));
    const serviceSpy = jest.spyOn(service, 'delete');

    await component.deleteConfig({
        caption: '',
        configuration: {overlays: [], productId: undefined, sceneId: undefined, date: '', viewPosition: undefined},
        thumbnail: '',
        uuid: 'uuid1'
      }
    );
    expect(serviceSpy).not.toHaveBeenCalled();
  });

  it('load config should redirect to url', () => {
    let mapSpy = spyOn(mapService, 'updateStoreByView').and.returnValue(of(null));
    const config: ViewRouterConfig = {
      overlays: [1, 2],
      productId: 13,
      sceneId: 14,
      date: '11-12-2020',
      viewPosition: {
        zoomLevel: 4,
        centerCoordinates: [10, 11]
      }
    };
    const viewConfig: ViewConfiguration = {
      caption: 'foo',
      configuration: config,
      thumbnail: null
    };

    component.loadConfig(viewConfig);
    expect(mapSpy).toHaveBeenCalledWith(config);
  });
});
