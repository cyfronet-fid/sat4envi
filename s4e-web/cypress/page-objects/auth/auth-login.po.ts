import {Core} from '../core.po';

export interface User {
  email: string;
  password: string;
}

export class Login extends Core {
  static readonly pageObject = {
    getLoginInput: () => cy.get('input[data-e2e="login-email-input"]'),
    getPasswordInput: () => cy.get('input[data-e2e="login-password-input"]'),
    getSubmitBtn: () => cy.get('button[data-e2e="submit-btn"]'),
    getGoToMapBtn: () => cy.get('a[data-e2e="go-to-map-btn"]'),
    getRegisterBtn: () => cy.get('[data-e2e="register-btn"]'),
    getPasswordResetBtn: () => cy.get('[data-e2e="password-reset"]'),
    getEmailInputBtn: () => cy.get('[data-e2e="email"]'),
    getNewPasswordInput: () => cy.get('[data-e2e="new-password"]'),
    getFieldErrors: () => cy.get('.invalid-feedback > .ng-star-inserted'),
    getError: () => cy.get('.message')
  };

  static fillForm(user: User) {
    Login.pageObject.getLoginInput().clear().type(user.email);
    Login.pageObject.getPasswordInput().clear().type(user.password);

    return Login;
  }

  static unfillForm() {
    Login.pageObject.getSubmitBtn().invoke('removeAttr', 'disabled').click();

    return Login;
  }

  static sendForm() {
    Login.pageObject.getSubmitBtn().click();

    return Login;
  }

  static errorsCountShouldBe(count: number) {
    cy.location('pathname').should('eq', '/login');

    Login.pageObject.getFieldErrors().should('have.length', count);

    return Login;
  }

  static hasErrorLogin() {
    cy.location('pathname').should('eq', '/login');

    Login.pageObject.getError().should('be.visible');

    return Login;
  }

  static loginAs(user: User) {
    cy.server();
    cy.route('GET', '/api/v1/users/me').as('me');

    Login.fillForm(user).sendForm();

    cy.wait('@me');

    cy.location('href').should('include', '/map/products?');

    return Login;
  }

  static forceLogout() {
    cy.server();
    cy.route({
      method: 'POST',
      url: '**/logout'
    }).as('@logoutRequest');
    cy.visit('/logout');
    cy.wait('@logoutRequest').wait(300);
  }

  static loginPageShouldNotBeAllowed() {
    cy.visit('/login');
    cy.url().should('not.contain', '/login');

    return Login;
  }

  static goToMap() {
    Login.pageObject.getGoToMapBtn().click();

    cy.location('href').should('include', '/map/products?');
  }

  static goToRegisterPage() {
    Login.pageObject.getRegisterBtn().click();

    cy.location('href').should('include', '/register');

    return Login;
  }

  static goToPasswordResetPage() {
    Login.pageObject.getPasswordResetBtn().click();

    cy.location('href').should('include', '/password-reset');

    return Login;
  }

  static enterEmailForResetPasswordLink(email: string) {
    Login.pageObject.getEmailInputBtn().type(email);

    Login.pageObject.getSubmitBtn().click();

    return Login;
  }

  static setNewPassword(password: string) {
    Login.pageObject.getNewPasswordInput().type(password);

    Login.pageObject.getSubmitBtn().click();

    return Login;
  }

  static clickResetPasswordLink() {
    cy.getAllMails()
      .filterBySubject('Reset')
      .should('have.length', 1)
      .firstMail()
      .getMailDocumentContent()
      .then(($document: Document) => {
        const resetPasswordUrl = Array.from($document.getElementsByTagName('a'))
          .map(el => el.href)
          .filter(href => href.includes('/password-reset'))
          .toString()
          .replace('=', '');

        cy.visit(resetPasswordUrl);
      });
  }
}
