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

import {PersonFactory} from '../state/person/person.factory.spec';
import {InvitationQuery} from '../state/invitation/invitation.query';
import {InstitutionService} from '../../state/institution/institution.service';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {PersonListComponent} from './person-list.component';
import {SettingsModule} from '../../settings.module';
import {DebugElement} from '@angular/core';
import {PersonQuery} from '../state/person/person.query';
import {PersonService} from '../state/person/person.service';
import {InvitationFactory} from '../state/invitation/invitation.factory.spec';
import {invitationToPerson} from '../state/invitation/invitation.model';

describe('PeopleListComponent', () => {
  let component: PersonListComponent;
  let fixture: ComponentFixture<PersonListComponent>;
  let personQuery: PersonQuery;
  let invitationQuery: InvitationQuery;
  let institutionService: InstitutionService;
  let personService: PersonService;
  let de: DebugElement;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [SettingsModule, RouterTestingModule, HttpClientTestingModule]
      }).compileComponents();

      invitationQuery = TestBed.inject(InvitationQuery);
      personQuery = TestBed.inject(PersonQuery);
      personService = TestBed.inject(PersonService);
      institutionService = TestBed.inject(InstitutionService);
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonListComponent);
    component = fixture.componentInstance;
    de = fixture.debugElement;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
  it('should display invitation buttons on invitation person', () => {
    spyOn(invitationQuery, 'selectLoading').and.returnValue(of(false));
    spyOn(personQuery, 'selectLoading').and.returnValue(of(false));

    const invitations = InvitationFactory.buildList(1).map(invitation =>
      invitationToPerson(invitation)
    );
    spyOn(invitationQuery, 'selectAll').and.returnValue(of([]));
    spyOn(personQuery, 'selectAll').and.returnValue(of(invitations));

    fixture.detectChanges();

    const resendBtn = de.query(By.css('button[data-ut="resend-btn"]'));
    expect(resendBtn).toBeTruthy();

    const deleteBtn = de.query(By.css('a[data-ut="delete-btn"]'));
    expect(deleteBtn).toBeTruthy();
  });
  it("shouldn't display invitation buttons on member", () => {
    spyOn(invitationQuery, 'selectLoading').and.returnValue(of(false));
    spyOn(personQuery, 'selectLoading').and.returnValue(of(false));
    spyOn(invitationQuery, 'selectAll').and.returnValue(of([]));

    const persons = PersonFactory.buildList(1);
    spyOn(personQuery, 'selectAll').and.returnValue(of(persons));

    fixture.detectChanges();

    const resendBtn = de.query(By.css('a[data-ut="resend-btn"]'));
    expect(resendBtn).toBeFalsy();

    const deleteBtn = de.query(By.css('a[data-ut="delete-btn"]'));
    expect(deleteBtn).toBeTruthy();
  });
  it("should delete invitation if it's invitation person", async () => {
    const invitation = InvitationFactory.build();
    const invitationAsPerson = invitationToPerson(invitation);
    const handleInvitationDeletionSpy = spyOn(
      component as any,
      '_handleInvitationDeletion'
    );
    const handlePersonDeletionSpy = spyOn(component as any, '_handleUserDeletion');

    await component.delete(invitationAsPerson);

    expect(handleInvitationDeletionSpy).toHaveBeenCalledWith(invitationAsPerson);
    expect(handlePersonDeletionSpy).not.toHaveBeenCalled();
  });
  it('should delete member of institution', async () => {
    const person = PersonFactory.build();
    const handleInvitationDeletionSpy = spyOn(
      component as any,
      '_handleInvitationDeletion'
    );
    const handlePersonDeletionSpy = spyOn(component as any, '_handleUserDeletion');

    await component.delete(person);

    expect(handleInvitationDeletionSpy).not.toHaveBeenCalled();
    expect(handlePersonDeletionSpy).toHaveBeenCalledWith(person);
  });
});
