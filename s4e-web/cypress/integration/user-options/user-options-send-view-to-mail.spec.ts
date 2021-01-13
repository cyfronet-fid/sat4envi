/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { UserOptionsSendView } from "../../page-objects/user-options/user-options-send-view-to-mail.po"


before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Send View', () => {
  beforeEach(function () {
    cy.visit("/login")
    Login.loginAs(this.zkMember);
  });

  it('should send view to mail', () => {
    UserOptionsSendView
      .openSendViewsModal()
      .fillFields("test@mail.pl", "caption-test", "description-test")
      .sendView()
  });
});

