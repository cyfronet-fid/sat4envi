import { Core } from '../core.po';

export interface User {
  email: string;
  password: string;
}

export class Login extends Core {

  static readonly pageObject = {
    getLoginInput: () => cy.get('input[data-e2e="login-email-input"]'),
    getPasswordInput: () => cy.get('input[data-e2e="login-password-input"]'),
    getSubmitBtn: () => cy.get('button[data-e2e="login-submit-btn"]'),
    getGoToMapBtn: () => cy.get('a[data-e2e="go-to-map-btn"]'),

    getFieldErrors: () => cy.get('.invalid-feedback > .ng-star-inserted'),
    getError: () => cy.get('.message')
  };

  static fillForm(user: User) {
    Login
      .pageObject
      .getLoginInput()
      .clear()
      .type(user.email);
    Login
      .pageObject
      .getPasswordInput()
      .clear()
      .type(user.password);

    return Login;
  }

  static unfillForm() {
    Login
      .pageObject
      .getSubmitBtn()
      .invoke("removeAttr", "disabled")
      .click()

    return Login;
  }

  static sendForm() {
    Login
      .pageObject
      .getSubmitBtn()
      .click();

    return Login;
  }


  static errorsCountShouldBe(count: number) {
    cy.location('pathname').should('eq', '/login');

    Login
      .pageObject
      .getFieldErrors()
      .should('have.length', count);

    return Login;
  }

  static hasErrorLogin() {
    cy.location('pathname').should('eq', '/login');

    Login
      .pageObject
      .getError();

    return Login;
  }

  static loginAs(user: User) {
    cy.server();
    cy.route('POST', "/api/v1/login").as("login");
    cy.route('GET', "/api/v1/users/me").as("me");

    Login
      .fillForm(user)
      .sendForm();

    cy.wait(5000)

    cy.wait('@login')
    cy.wait('@me')

    cy.wait(5000)
    
    cy.location('href').should('include', '/map/products?');

    return Login;
  }

  static forceLogout() {
    cy.server();
    cy.route({
      method: 'POST',
      url: '**/logout'
    })
      .as('@logoutRequest');
    cy.visit('/logout');
    cy.wait('@logoutRequest')
      .wait(300);
  }

  static loginPageShouldNotBeAllowed() {
    cy.visit('/login')
    cy.url().should('not.contain', '/login');

    return Login;
  }

  static goToMap() {

    Login
      .pageObject
      .getGoToMapBtn()
      .click()

    cy.location('href').should('include', '/map/products?');
  }
};
