import { User } from '../auth/auth-login.po'
import { Core } from '../core.po';
import promisify from 'cypress-promise';

export class JwtTokenModal extends Core {
  static pageObject = {
    getPasswordInput: () => cy.get('[data-e2e="jwt-token-password-input"]')
      .find('input'),
    getLoadTokenBtn: () => cy.get('[data-e2e="load-jwt-token-btn"]'),
    getTokenTextarea: () => cy.get('[data-e2e="jwt-token-txt"]'),
    getCopyToClipboardBtn: () => cy.get('[data-e2e="copy-to-clipboard"]'),
    getApiManual: () => cy.get('a').contains("Przeczytaj przewodnik")
  };

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

  static goToApiManula(){
      JwtTokenModal
      .pageObject
      .getApiManual()
      .invoke('removeAttr', 'target')
      .click()

      cy.location('pathname').should('eq', '/howto')

      return JwtTokenModal
  }
}
