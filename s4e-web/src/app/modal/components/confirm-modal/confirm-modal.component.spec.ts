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
import {ConfirmModalComponent} from './confirm-modal.component';
import {By} from '@angular/platform-browser';
import {ModalModule} from '../../modal.module';
import {MODAL_DEF} from '../../modal.providers';
import {createModal} from '../../state/modal.model';
import {ALERT_MODAL_ID} from '../alert-modal/alert-modal.model';
import {CONFIRM_MODAL_ID, ConfirmModal} from './confirm-modal.model';
import {ModalService} from '../../state/modal.service';

describe('ConfirmModalComponent', () => {
  let component: ConfirmModalComponent;
  let compiled: HTMLElement;
  let service: ModalService;
  let fixture: ComponentFixture<ConfirmModalComponent>;
  const content = 'CNT';
  const title = 'TITLE';

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ModalModule],
      providers: [
        {provide: MODAL_DEF, useValue: createModal({id: CONFIRM_MODAL_ID, size: 'sm', content, title} as ConfirmModal)}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmModalComponent);
    component = fixture.componentInstance;
    service = TestBed.get(ModalService);
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

  it('should call dismiss(true) on accept', () => {
    const spy = spyOn(component, 'dismiss').and.stub();
    component.accept();
    expect(spy).toHaveBeenCalledWith(true);
  });

  // DOM TESTING
  it('should call dismiss after clicking Cancel', () => {
    let dismissSpy = jest.spyOn(component, 'dismiss');
    fixture.debugElement.query(By.css('#cancel_btn')).nativeElement.click();
    expect(dismissSpy).toHaveBeenCalled();
  });

  // DOM TESTING
  it('should call accept after clicking Accept', () => {
    let acceptSpy = jest.spyOn(component, 'accept');
    fixture.debugElement.query(By.css('#accept_btn')).nativeElement.click();
    expect(acceptSpy).toHaveBeenCalled();
  });
});
