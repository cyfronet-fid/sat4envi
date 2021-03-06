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

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {SessionService} from '../../state/session/session.service';
import {RouterTestingModule} from '@angular/router/testing';
import {LogoutModule} from './logout.module';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {LogoutComponent} from './logout.component';

describe('LogoutComponent', () => {
  let component: LogoutComponent;
  let fixture: ComponentFixture<LogoutComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [LogoutModule, RouterTestingModule, HttpClientTestingModule]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(LogoutComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should logout and run notification', () => {
    const sessionService = TestBed.inject(SessionService);
    const spySession = spyOn(sessionService, 'logout');

    fixture.detectChanges();

    expect(spySession).toHaveBeenCalled();
  });
});
