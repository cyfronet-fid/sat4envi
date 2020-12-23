/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';
import { MailToExpert } from '../../page-objects/user-options/user-options-mail-to-expert.po'

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe("Mail to Expert", () => {
 
  beforeEach(function () {
    cy.visit("/login")
    Login
      .loginAs(this.zkMember)
  });

  it("Should send mail to expert", () => {
    MailToExpert
      .openSendMailToExpertModal()
      .addMessageToSupport('Wsparcie zdalne',"Test Message")
      .sendMessageToSupport()
    ConfirmModal
      .accept()
    MailToExpert
      .confirmationShouldToAppear()
  })
});