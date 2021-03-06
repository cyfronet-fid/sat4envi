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

import {PeopleModule} from '../people.module';
import {RouterTestingModule} from '@angular/router/testing';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap, ParamMap} from '@angular/router';
import {InvitationFormComponent} from './invitation-form.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MODAL_DEF} from 'src/app/modal/modal.providers';
import {
  INVITATION_FORM_MODAL_ID,
  InvitationFormModal
} from './invitation-form-modal.model';
import {ReplaySubject, Subject} from 'rxjs';
import {InvitationService} from '../state/invitation/invitation.service';

class ActivatedRouteStub {
  queryParamMap: Subject<ParamMap>;

  constructor() {
    this.queryParamMap = new ReplaySubject(1);
    this.queryParamMap.next(convertToParamMap({}));
  }
}

describe('InvitationFormComponent', () => {
  let component: InvitationFormComponent;
  let fixture: ComponentFixture<InvitationFormComponent>;
  let invitationService: InvitationService;
  let route: ActivatedRouteStub;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [PeopleModule, HttpClientTestingModule, RouterTestingModule],
        providers: [
          {provide: ActivatedRoute, useClass: ActivatedRouteStub},
          {
            provide: MODAL_DEF,
            useValue: {
              id: INVITATION_FORM_MODAL_ID,
              size: 'lg',
              institution: {name: 'test', slug: 'test'}
            } as InvitationFormModal
          }
        ]
      });
      invitationService = TestBed.inject(InvitationService);
      fixture = TestBed.createComponent(InvitationFormComponent);
      component = fixture.componentInstance;
      route = (<unknown>TestBed.inject(ActivatedRoute)) as ActivatedRouteStub;
    })
  );

  it('can load instance', () => {
    expect(component).toBeTruthy();
  });
  it('should not call invitation create', () => {
    const name = 'test';
    const slug = 'test';
    component.institution = {name, slug} as any;

    fixture.detectChanges();

    spyOn(invitationService, 'send').and.callThrough();
    component.send();
    expect(invitationService.send).not.toHaveBeenCalled();
  });
  it('should call invitation send', () => {
    const name = 'test';
    const slug = 'test';
    component.institution = {name, slug} as any;

    fixture.detectChanges();

    const email = 'test@mail.pl';
    component.form.patchValue({email});

    spyOn(invitationService, 'send').and.callThrough();
    component.send();
    expect(invitationService.send).toHaveBeenCalledWith(slug, email, false);
  });
  it('should call invitation resend', () => {
    const name = 'test';
    const slug = 'test';
    component.institution = {name, slug} as any;

    const email = 'test@mail.pl';
    const status = 'waiting';
    component.invitation = {email, status} as any;

    fixture.detectChanges();
    component.form.patchValue({email});

    spyOn(invitationService, 'resend').and.callThrough();
    component.send();
    expect(invitationService.resend).toHaveBeenCalledWith(
      {forAdmin: false, newEmail: email, oldEmail: email},
      component.institution
    );
  });
});
