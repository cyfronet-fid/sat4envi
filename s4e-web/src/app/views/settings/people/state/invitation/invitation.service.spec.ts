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

import { InvitationStore } from './invitation.store';
import { InvitationQuery } from './invitation.query';
import { InstitutionFactory } from './../../../state/institution/institution.factory.spec';
import { InvitationFactory } from './invitation.factory.spec';
import { TestBed, async } from '@angular/core/testing';

import { InvitationService } from './invitation.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import environment from 'src/environments/environment';

describe('InvitationService', () => {
  let service: InvitationService;
  let http: HttpTestingController;
  let query: InvitationQuery;
  let store: InvitationStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
      ]
    });
    TestBed.configureTestingModule({});

    service = TestBed.get(InvitationService);
    http = TestBed.get(HttpTestingController);
    query = TestBed.get(InvitationQuery);
    store = TestBed.get(InvitationStore);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
  it('should resend invitation and replace it with new one', () => {
    const invitation = InvitationFactory.build();
    store.set([]);
    store.add(invitation);

    const newInvitation = InvitationFactory.build();
    const institution = InstitutionFactory.build();
    service.resend({oldEmail: invitation.email, newEmail: newInvitation.email}, institution);

    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/invitations`;
    const method = 'PUT';
    const req = http.expectOne({method, url});
    req.flush(newInvitation);

    query.selectAll()
      .subscribe((invitations) => expect(invitations).toBeEqual([newInvitation]));
  });
  it('should delete invitation and remove it from store', () => {
    const invitation = InvitationFactory.build();
    store.set([]);
    store.add(invitation);

    const institution = InstitutionFactory.build();
    service.delete(invitation, institution);

    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/invitations/${invitation.id}`;
    const method = 'DELETE';
    const req = http.expectOne({method, url});
    req.flush({});

    query.selectAll()
      .subscribe((invitations) => expect(invitations).toBeEqual([]));
  });
  it('should send invitation and add it into store', () => {
    store.set([]);

    const newInvitation = InvitationFactory.build();
    const institution = InstitutionFactory.build();
    service.send(institution.slug, newInvitation.email).subscribe();

    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/invitations`;
    const method = 'POST';
    const req = http.expectOne({method, url});
    req.flush(newInvitation);

    query.selectAll()
      .subscribe((invitations) => expect(invitations).toBeEqual([newInvitation]));
  });
});
