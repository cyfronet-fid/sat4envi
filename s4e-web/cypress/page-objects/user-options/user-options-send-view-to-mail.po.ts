import { Core } from '../core.po';

export class SendView extends Core{
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOpenSendViewToMail: () => cy.get('[data-e2e="open-send-view-btn"]'), 
    getEmailsInput: () => cy.get('[data-e2e="emails"]'),
    getContainInput: () => cy.get('[data-e2e="caption"]'),
    getDescriptionInput: () => cy.get('[data-e2e="description"]'),
    getSendBtn: () => cy.get('[data-e2e="btn-submit"]'),

    getMessage: () => cy.get(".message")
  };

  static openSendViewsModal() {
    SendView
      .pageObject
      .getOptionsBtn()
      .click();

    SendView
      .pageObject
      .getOpenSendViewToMail()
      .click();

    return SendView;
  }

  static fillFields(email:string, caption:string, description:string){
    SendView
      .pageObject
      .getEmailsInput()
      .type(email);

    SendView
      .pageObject
      .getContainInput()
      .type(caption);
    
    SendView
      .pageObject
      .getDescriptionInput()
      .type(description);

    return SendView;
  }

  static sendView(){
    SendView
      .pageObject
      .getSendBtn()
      .click();

    SendView
      .pageObject
      .getMessage()
      .should("be.visible");
      
    return SendView;
  }
}