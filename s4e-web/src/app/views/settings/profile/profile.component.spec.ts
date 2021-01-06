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

import { InstitutionFactory } from './../state/institution/institution.factory.spec';
import { of } from 'rxjs';
import { InstitutionQuery } from './../state/institution/institution.query';
import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {SessionService} from '../../../state/session/session.service';
import {ProfileComponent} from './profile.component';
import {SessionQuery} from '../../../state/session/session.query';
import {SessionStore} from '../../../state/session/session.store';
import {ProfileModule} from './profile.module';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import {ModalService} from '../../../modal/state/modal.service';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let sessionService: SessionService;
  let sessionQuery: SessionQuery;
  let sessionStore: SessionStore;
  let institutionQuery: InstitutionQuery;
  let modalService: ModalService;
  let de: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ProfileModule,
        HttpClientTestingModule,
        RouterTestingModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
    sessionService = TestBed.get(SessionService);
    sessionQuery = TestBed.get(SessionQuery);
    sessionStore = TestBed.get(SessionStore);
    modalService = TestBed.get(ModalService);

    institutionQuery = TestBed.get(InstitutionQuery);
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display institutions list', () => {
    const institutions = InstitutionFactory.buildList(5);
    component.institutions$ = of(institutions);

    fixture.detectChanges();

    const institutionsList = de.query(By.css('[data-ut="institutions"]'));
    expect(institutionsList).toBeTruthy();
  });
  it('should hide institutions list', () => {
    component.institutions$ = of([]);

    fixture.detectChanges();

    const institutionsList = de.query(By.css('[data-ut="institutions"]'));
    expect(institutionsList).toBeFalsy();
  });
  it('should remove user', async () => {
    const password = 'test1234';
    component.form.patchValue({password});

    const email = 'test@mail.lp';
    spyOn(sessionQuery, 'getValue').and.returnValue({email});
    spyOn(modalService, 'confirm').and.returnValue(of(true).toPromise());

    const spy = spyOn(sessionService, 'removeAccount$').and.returnValue(of());

    await component.removeAccount();

    expect(spy).toHaveBeenCalledWith(email, password);
  });
  it('should valid remove user', async () => {
    const email = 'test@mail.lp';
    spyOn(sessionQuery, 'getValue').and.returnValue({email});
    spyOn(modalService, 'confirm').and.returnValue(of(true).toPromise());

    const spy = spyOn(sessionService, 'removeAccount$');

    await component.removeAccount();

    expect(spy).not.toHaveBeenCalled();
  });
  it('should not remove user', async () => {
    const password = 'test1234';
    component.form.patchValue({password});

    const email = 'test@mail.lp';
    spyOn(sessionQuery, 'getValue').and.returnValue({email});
    spyOn(modalService, 'confirm').and.returnValue(of(false).toPromise());

    const spy = spyOn(sessionService, 'removeAccount$');

    await component.removeAccount();

    expect(spy).not.toHaveBeenCalled();
  });
});
