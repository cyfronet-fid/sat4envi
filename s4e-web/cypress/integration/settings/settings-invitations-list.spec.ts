/// <reference types="Cypress" />

import { Login } from '../../page-objects/login/login.po';
import { InstitutionPeople } from '../../page-objects/settings/settings-institution-people.po';
import { Core } from './../../page-objects/core.po';

context('Settings invitations list', () => {
  beforeEach(() => {
    cy.fixture('users/zkAdmin.json').as('zkAdmin');
    cy.wrap('test0@user.pl').as('testEmail');
    cy.wrap('test1@user.pl').as('newTestEmail');
  });

  beforeEach(function () {
    Login
      .loginAs(this.zkAdmin)
      .goToSettingsAs(this.zkAdmin)
      .goToNthInstitutionPeople(0);
  });
  it('should add and remove', function () {
    InstitutionPeople
      .addInvitation(this.testEmail)
      .invitationsCountShouldBe(1)
      .invitationWithEmailShouldExist(this.testEmail)
      .removeBy(this.testEmail)
      .invitationsCountShouldBe(0);
  });
  // TODO When requests in cypress will be made good enough
  // it('should resend', function () {
  //   // TODO: extract responses/requests check functionality
  //   // TODO: extract conditional functions
  //   // click resend
  //   // resend should return code 200 and response with invitation
  // });
  it('should re-edit', function () {
    InstitutionPeople
      .addInvitation(this.testEmail)
      .invitationsCountShouldBe(1)
      .invitationWithEmailShouldExist(this.testEmail)
      .resendToNewEmail(this.testEmail, this.newTestEmail)
      .invitationsCountShouldBe(1)
      .invitationWithEmailShouldExist(this.newTestEmail)
      .removeBy(this.newTestEmail)
      .invitationsCountShouldBe(0);
  });
});
