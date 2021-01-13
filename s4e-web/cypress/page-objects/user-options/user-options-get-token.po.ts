import { User } from '../auth/auth-login.po'
import { Core } from '../core.po';

export class JwtTokenModal extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOptionsDropdown: () => cy.get('[data-e2e="options-dropdown"]'),
    getApiBtn:()=> cy.get('[data-e2e="open-jwt-token-btn"]'),
    getPasswordInput: () => cy.get('[data-e2e="jwt-token-password-input"]')
      .find('input'),
    getLoadTokenBtn: () => cy.get('[data-e2e="load-jwt-token-btn"]'),
    getTokenTextarea: () => cy.get('[data-e2e="jwt-token-txt"]'),
    getCopyToClipboardBtn: () => cy.get('[data-e2e="copy-to-clipboard"]'),
    getApiManual: () => cy.get('[data-e2e="howTo"]')
  };

  static openJwtTokenModal(){
    JwtTokenModal
    .pageObject
    .getOptionsBtn()
    .should("be.visible")
    .click()

    JwtTokenModal
    .pageObject
    .getApiBtn()
    .click()

    return JwtTokenModal;
  }

  static authenticateAs(user: User) {
    JwtTokenModal
      .pageObject
      .getPasswordInput()
      .should('be.visible')
      .type(user.password);

    JwtTokenModal
      .pageObject
      .getLoadTokenBtn()
      .should('be.visible')
      .click();

    return JwtTokenModal;
  }

  static tokenShouldContain(text: string) {
    JwtTokenModal
      .pageObject
      .getTokenTextarea()
      .should('be.visible')
      .invoke('text')
      .should('contain', text);

    return JwtTokenModal;
  }

  static shouldHaveVisibleToken() {
    JwtTokenModal
      .pageObject
      .getTokenTextarea()
      .should('be.visible');

    return JwtTokenModal;
  }

  static goToApiManual(){
      JwtTokenModal
      .pageObject
      .getApiManual()
      .invoke('removeAttr', 'target')
      .click()

      cy.location('pathname').should('eq', '/howto')

      return JwtTokenModal;
  }
}