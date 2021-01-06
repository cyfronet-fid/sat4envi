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
import {AlertModalComponent} from './alert-modal.component';
import {By} from '@angular/platform-browser';
import {ModalModule} from '../../modal.module';
import {MODAL_DEF} from '../../modal.providers';
import {createModal} from '../../state/modal.model';
import {ALERT_MODAL_ID, AlertModal} from './alert-modal.model';
import {ModalService} from '../../state/modal.service';

describe('AlertModalComponent', () => {
  let component: AlertModalComponent;
  let compiled: HTMLElement;
  let service: ModalService;
  let fixture: ComponentFixture<AlertModalComponent>;
  const content = 'CNT';
  const title = 'TITLE';

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ModalModule],
      providers: [
        {provide: MODAL_DEF, useValue: createModal({id: ALERT_MODAL_ID, size: 'sm', content, title} as AlertModal)}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AlertModalComponent);
    service = TestBed.get(ModalService);
    component = fixture.componentInstance;
    compiled = fixture.debugElement.nativeElement;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title and content', () => {
    expect(compiled.querySelector('.s4e-modal-header').textContent).toContain(title);
    expect(compiled.querySelector('.s4e-modal-body').textContent).toContain(content);
  });

  // DOM TESTING
  it('should call accept after clicking Accept', async () => {
    let acceptSpy = jest.spyOn(component, 'dismiss');
    fixture.debugElement.query(By.css('#accept_btn')).nativeElement.click();
    expect(acceptSpy).toHaveBeenCalled();
  });
});
