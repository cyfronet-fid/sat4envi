import { User } from '../auth/login.po';
import { Core } from '../core.po';
import promisify from 'cypress-promise';

export class JwtTokenModal extends Core {
  static pageObject = {
    getPasswordInput: () => cy.get('[data-e2e="jwt-token-password-input"]')
      .find('input'),
    getLoadTokenBtn: () => cy.get('[data-e2e="load-jwt-token-btn"]'),
    getTokenTextarea: () => cy.get('[data-e2e="jwt-token-txt"]'),
    getCopyToClipboardBtn: () => cy.get('[data-e2e="copy-to-clipboard"]')
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
}
