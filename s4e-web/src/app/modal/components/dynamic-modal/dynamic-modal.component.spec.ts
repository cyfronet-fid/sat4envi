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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {DynamicModalComponent} from './dynamic-modal.component';
import {ModalModule} from '../../modal.module';
import {createModal} from '../../state/modal.model';
import {DUMMY_MODAL_ID} from '../dummy-modal/dummy-modal.model';
import {DummyModalComponent} from '../dummy-modal/dummy-modal.component';

describe('DynamicModalComponent', () => {
  let component: DynamicModalComponent;
  let fixture: ComponentFixture<DynamicModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ModalModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DynamicModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create modal from definition', () => {
    component.modal = createModal({id: DUMMY_MODAL_ID});
    fixture.detectChanges();
    expect(component.componentRef).toBeTruthy();
    expect(component.componentRef.instance).toBeInstanceOf(DummyModalComponent);
    expect((component.componentRef.instance as DummyModalComponent).registeredId).toEqual(DUMMY_MODAL_ID)
  });
});
