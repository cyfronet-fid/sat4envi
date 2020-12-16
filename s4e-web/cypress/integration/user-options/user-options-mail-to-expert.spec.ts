/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';
import { MailToExpert } from '../../page-objects/user-options/user-options-mail-toexpert.po'

describe.skip("Mail to Expert", () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login
      .loginAs(this.zkMember)
  });

  it("Should send mail to expert", () => {
    MailToExpert
      .openSendMailToExpertModal()
      .addMessageToSupport('Wsparcie zdalne',"Test Message")
      .sendMessageToSupport()
      .changeContextTo(ConfirmModal)
      .acceptAndChangeContextTo(MailToExpert)
      .confirmationShouldToAppear()
  })
});