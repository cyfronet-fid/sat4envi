import { Core } from '../core.po';


export class UserOptionsMailToExpert extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getSendMailToExpertBtn: () => cy.get('[data-e2e="open-mail-to-expert-modal-btn"]'),
    getSupportTypeInput: () => cy.get('[data-e2e="helpType"]'),
    getTextArea: () => cy.get('[data-e2e="issueDescription"]'),
    getSendBtn: () => cy.get('[data-e2e="btn-submit"]'),
    getConfirmationMessage: () => cy.get(".message")
  };

  static openSendMailToExpertModal() {
    UserOptionsMailToExpert
      .pageObject
      .getOptionsBtn()
      .click()
    UserOptionsMailToExpert
      .pageObject
      .getSendMailToExpertBtn()
      .click()

    return UserOptionsMailToExpert;
  }

  static addMessageToSupport(select: string, message: string) {
    UserOptionsMailToExpert
      .pageObject
      .getSupportTypeInput()
      .select(select)

    UserOptionsMailToExpert
      .pageObject
      .getTextArea()
      .type(message)

    return UserOptionsMailToExpert;
  };

  static sendMessageToSupport() {
    UserOptionsMailToExpert
      .pageObject
      .getSendBtn()
      .click()

    return UserOptionsMailToExpert;
  };

  static confirmationShouldToAppear() {
    UserOptionsMailToExpert
      .pageObject
      .getConfirmationMessage()
      .should("be.visible");

    return UserOptionsMailToExpert;
  };
};