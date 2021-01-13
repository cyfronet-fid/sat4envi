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
import {DummyModalComponent} from './dummy-modal.component';
import {ModalModule} from '../../modal.module';
import {MODAL_DEF} from '../../modal.providers';
import {createModal} from '../../state/modal.model';
import {ALERT_MODAL_ID} from '../alert-modal/alert-modal.model';

describe('DummyModalComponent', () => {
  let component: DummyModalComponent;
  let fixture: ComponentFixture<DummyModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ModalModule],
      providers: [
        {provide: MODAL_DEF, useValue: createModal({id: ALERT_MODAL_ID})}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DummyModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
