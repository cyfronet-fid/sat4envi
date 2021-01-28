/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';
import { UserOptionsMailToExpert } from '../../page-objects/user-options/user-options-mail-to-expert.po'

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe("Mail to Expert", () => {

  beforeEach(function () {
    cy.visit("/login")
    Login
      .loginAs(this.zkMember)
  });

  it("should send mail to expert", () => {
    UserOptionsMailToExpert
      .openSendMailToExpertModal()
      .addMessageToSupport('Wsparcie zdalne', "Test Message")
      .sendMessageToSupport()
    ConfirmModal
      .accept()
    UserOptionsMailToExpert
      .confirmationShouldToAppear()
  })
});