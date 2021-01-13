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

import {of} from 'rxjs';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ChangePasswordComponent} from './change-password.component';
import {SessionService} from '../../../../state/session/session.service';
import {ProfileModule} from '../profile.module';

describe('ChangePasswordComponent', () => {
  let component: ChangePasswordComponent;
  let fixture: ComponentFixture<ChangePasswordComponent>;
  let sessionService: SessionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ProfileModule,
        RouterTestingModule,
        HttpClientTestingModule
      ]
    })
      .compileComponents();

    sessionService = TestBed.get(SessionService);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not send non valid form', () => {
    const spy = spyOn(sessionService, 'changePassword');
    component.submitPasswordChange();
    expect(spy).not.toHaveBeenCalled();
  });

  it('should send valid form', () => {
    const spy = spyOn(sessionService, 'changePassword').and.returnValue(of(1));
    const oldPassword = 'zkMember';
    const newPassword = 'ZKMEMBER';
    component.form
      .setValue({oldPassword, newPassword});
    component.submitPasswordChange();
    expect(spy).toHaveBeenCalledWith(oldPassword, newPassword);
  });

  it('should validate old password', () => {
    component.form.controls.oldPassword.setValue('');
    expect(component.form.controls.oldPassword.valid).toBeFalsy();
    component.form.controls.oldPassword.setValue('password');
    expect(component.form.controls.oldPassword.valid).toBeTruthy();
  });

  it('should validate new password', () => {
    component.form.controls.newPassword.setValue('');
    expect(component.form.controls.newPassword.valid).toBeFalsy();
    component.form.controls.newPassword.setValue('password');
    expect(component.form.controls.newPassword.valid).toBeTruthy();
  });
});
