import { Core } from '../core.po';


export class MailToExpert extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getSendMailToExpertBtn: () => cy.get('[data-e2e="open-mail-to-expert-modal-btn"]'),
    getSupportTypeInput: () => cy.get('[data-e2e="helpType"]'),
    getTextArea: () => cy.get('[data-e2e="issueDescription"]'),
    getSendBtn: () => cy.get('[data-e2e="btn-submit"]'),
    getConfirmationMessage: () => cy.get(".message")
  };

  static openSendMailToExpertModal() {
    MailToExpert
      .pageObject
      .getOptionsBtn()
      .click()
    MailToExpert
      .pageObject
      .getSendMailToExpertBtn()
      .click()

    return MailToExpert;
  }

  static addMessageToSupport(select: string, message: string) {
    MailToExpert
      .pageObject
      .getSupportTypeInput()
      .select(select)

    MailToExpert
      .pageObject
      .getTextArea()
      .type(message)

    return MailToExpert;
  };

  static sendMessageToSupport() {
    MailToExpert
      .pageObject
      .getSendBtn()
      .click()

    return MailToExpert;
  };

  static confirmationShouldToAppear() {
    MailToExpert
      .pageObject
      .getConfirmationMessage()
      .should("be.visible");

    return MailToExpert;
  };
};