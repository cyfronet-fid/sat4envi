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
    service.send(institution.slug, newInvitation.email);

    const url = `${environment.apiPrefixV1}/institutions/${institution.slug}/invitations`;
    const method = 'POST';
    const req = http.expectOne({method, url});
    req.flush(newInvitation);

    query.selectAll()
      .subscribe((invitations) => expect(invitations).toBeEqual([newInvitation]));
  });
});
