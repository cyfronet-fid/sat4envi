import { User } from '../auth/auth-login.po'
import { Core } from '../core.po';

export class UserOptionsTokenModal extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOptionsDropdown: () => cy.get('[data-e2e="options-dropdown"]'),
    getApiBtn: () => cy.get('[data-e2e="open-jwt-token-btn"]'),
    getPasswordInput: () => cy.get('[data-e2e="jwt-token-password-input"]')
      .find('input'),
    getLoadTokenBtn: () => cy.get('[data-e2e="load-jwt-token-btn"]'),
    getTokenTextarea: () => cy.get('[data-e2e="jwt-token-txt"]'),
    getCopyToClipboardBtn: () => cy.get('[data-e2e="copy-to-clipboard"]'),
    getApiManual: () => cy.get('[data-e2e="howTo"]')
  };

  static openJwtTokenModal() {
    UserOptionsTokenModal
      .pageObject
      .getOptionsBtn()
      .should("be.visible")
      .click()

    UserOptionsTokenModal
      .pageObject
      .getApiBtn()
      .click()

    return UserOptionsTokenModal;
  }

  static authenticateAs(user: User) {
    UserOptionsTokenModal
      .pageObject
      .getPasswordInput()
      .should('be.visible')
      .type(user.password);

    UserOptionsTokenModal
      .pageObject
      .getLoadTokenBtn()
      .should('be.visible')
      .click();

    return UserOptionsTokenModal;
  }

  static tokenShouldContain(text: string) {
    UserOptionsTokenModal
      .pageObject
      .getTokenTextarea()
      .should('be.visible')
      .invoke('text')
      .should('contain', text);

    return UserOptionsTokenModal;
  }

  static shouldHaveVisibleToken() {
    UserOptionsTokenModal
      .pageObject
      .getTokenTextarea()
      .should('be.visible');

    return UserOptionsTokenModal;
  }

  static goToApiManual() {
    UserOptionsTokenModal
      .pageObject
      .getApiManual()
      .invoke('removeAttr', 'target')
      .click()

    cy.location('pathname').should('eq', '/howto')

    return UserOptionsTokenModal;
  }
}