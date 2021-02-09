import {Core} from '../core.po';

export class UserOptionsSendView extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOpenSendViewToMail: () => cy.get('[data-e2e="open-send-view-btn"]'),
    getEmailsInput: () => cy.get('[data-e2e="emails"]'),
    getContainInput: () => cy.get('[data-e2e="caption"]'),
    getDescriptionInput: () => cy.get('[data-e2e="description"]'),
    getSendBtn: () => cy.get('[data-e2e="btn-submit"]'),

    getMessage: () => cy.get('.message')
  };

  static openSendViewsModal() {
    UserOptionsSendView.pageObject.getOptionsBtn().click();

    UserOptionsSendView.pageObject.getOpenSendViewToMail().click();

    return UserOptionsSendView;
  }

  static fillFields(email: string, caption: string, description: string) {
    UserOptionsSendView.pageObject.getEmailsInput().type(email);

    UserOptionsSendView.pageObject.getContainInput().type(caption);

    UserOptionsSendView.pageObject.getDescriptionInput().type(description);

    return UserOptionsSendView;
  }

  static sendView() {
    UserOptionsSendView.pageObject.getSendBtn().click();

    UserOptionsSendView.pageObject.getMessage().should('be.visible');

    return UserOptionsSendView;
  }
}
