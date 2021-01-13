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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetPasswordComponent } from './reset-password.component';
import {RouterTestingModule} from '@angular/router/testing';
import {ActivatedRoute, convertToParamMap, ParamMap, Router} from '@angular/router';
import {of, ReplaySubject, Subject} from 'rxjs';
import {SessionService} from '../../state/session/session.service';
import {ResetPasswordModule} from './reset-password.module';

class ActivatedRouteStub {
  paramMap: Subject<any> = new ReplaySubject(1);

  constructor() {
    this.paramMap.next(convertToParamMap({}));
  }
}

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let activatedRoute: ActivatedRouteStub;
  let router: Router;
  let sessionService: SessionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ResetPasswordModule,
        RouterTestingModule
      ],
      providers: [{provide: ActivatedRoute, useClass: ActivatedRouteStub}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetPasswordComponent);
    activatedRoute = TestBed.get(ActivatedRoute);
    router = TestBed.get(Router);
    sessionService = TestBed.get(SessionService);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should send password reset URL to the email', () => {
    const spySendPasswordResetToken = spyOn(sessionService, 'sendPasswordResetToken$')
      .and.returnValue(of());
    activatedRoute.paramMap.next(convertToParamMap({}));
    const email = 'correctEmail@mail.com';
    component.sendTokenForm.patchValue({email});

    component.sendPasswordResetToken();
    expect(spySendPasswordResetToken).toHaveBeenCalledWith(email);
  });

  it('should reset password', () => {
    const spyResetPassword = spyOn(sessionService, 'resetPassword$')
      .and.returnValue(of());
    const token = 'test1';
    activatedRoute.paramMap.next(convertToParamMap({token}));
    const password = 'correctPass123';
    component.resetPasswordForm.patchValue({password});

    component.resetPassword();
    expect(spyResetPassword).toHaveBeenCalledWith(token, password);
  });
});
