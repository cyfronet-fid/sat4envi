/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';
import { MapOptions } from '../../page-objects/map/map-options.po'

describe.skip("Map Options", () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login
      .loginAs(this.zkMember)
  });

  it("Should send mail to expert", () => {
    MapOptions
      .openSendMailToExpertModal()
      .addMessageToSupport('Wsparcie zdalne',"Test Message")
      .sendMessageToSupport()
      .changeContextTo(ConfirmModal)
      .acceptAndChangeContextTo(MapOptions)
      .confirmationShouldToAppear()
  })
});