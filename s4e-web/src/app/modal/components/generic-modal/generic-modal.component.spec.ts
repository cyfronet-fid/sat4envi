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
import {ModalModule} from '../../modal.module';
import {Component} from '@angular/core';
import {ModalService} from '../../state/modal.service';
import {By} from '@angular/platform-browser';

@Component({
  selector: 's4e-generic-mock-component',
  template: `
      <s4e-generic-modal [buttonX]="buttonX" [modalId]="registeredId" (close)="close()">
          <div class="s4e-modal-header">Mock Component</div>
          <div class="s4e-modal-body">
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc eleifend sit amet lacus in luctus.
          </div>
          <div class="s4e-modal-footer">
              <button class="button button--primary" type="submit" (click)="dismiss()" i18n>Ok</button>
          </div>
      </s4e-generic-modal>
  `
})
export class GenericModalMockComponent {
  buttonX = true;
  registeredId = '123';
  dismiss() {}
  close() {}
}

describe('GenericModalComponent', () => {
  let component: GenericModalMockComponent;
  let service: ModalService;
  let fixture: ComponentFixture<GenericModalMockComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ModalModule
      ],
      declarations: [
        GenericModalMockComponent
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GenericModalMockComponent);
    component = fixture.componentInstance;
    service = TestBed.get(ModalService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('clicking X should call ModalService.hide', () => {
    const spy = spyOn(service, 'hide').and.callThrough();
    fixture.debugElement.query(By.css('button.close')).nativeElement.click();
    expect(spy).toHaveBeenCalledWith(component.registeredId);
  });

  it('clicking X should call call (close)', () => {
    const spy = spyOn(service, 'hide').and.callThrough();
    fixture.debugElement.query(By.css('button.close')).nativeElement.click();
    expect(spy).toHaveBeenCalledWith(component.registeredId);
  });

  it('clicking x should not call ModalService.hide if modalId is not defined', () => {
    const spy = spyOn(component, 'close');
    fixture.debugElement.query(By.css('button.close')).nativeElement.click();
    expect(spy).toHaveBeenCalled();
  });

  it('if buttonX is false x button should not be shown', () => {
    component.buttonX = false;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('button.close'))).toBeFalsy();
  });

  it('dismiss should call xclicked', (done) => {
    const xclicked = () => done();

    fixture.debugElement.query(By.css('s4e-generic-modal')).componentInstance.xclicked = xclicked;
    fixture.debugElement.query(By.css('button.close')).nativeElement.click();
  });
});
