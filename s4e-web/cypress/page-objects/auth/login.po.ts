import { Map } from '../map/map.po';
import { Core } from '../core.po';
import { GeneralModal } from '../modal/general-modal.po';

export interface User {
  email: string;
  password: string;
}

export class Login extends Core {
  static readonly pageObject = {
    getLoginInput: () => cy.get('input[data-test="login-email-input"]'),
    getPasswordInput: () => cy.get('input[data-test="login-password-input"]'),
    getSubmitBtn: () => cy.get('button[data-test="login-submit-btn"]'),
    getGoToMapBtn: () => cy.get('a[data-test="go-to-map-btn"]'),

    getFieldErrors: () => cy.get('.invalid-feedback > .ng-star-inserted'),
    getError: () => cy.get('.alert.alert-danger.special__error').contains("Niepoprawny login / hasło")
  };

  static fillForm(user: User) {
    Login
      .pageObject
      .getLoginInput()
      .type(user.email);
    Login
      .pageObject
      .getPasswordInput()
      .type(user.password);

    return Login;
  }

  static unfillForm(){
    Login
      .pageObject
      .getLoginInput().click()
      
    Login
      .pageObject
      .getPasswordInput().click()

    cy.get("body").click();
    Login.pageObject.getSubmitBtn().should('be.disabled')

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
  }

  static loginAs(user: User) {
    // force logout
    cy.clearLocalStorage('s4eStore');

    cy.visit('/login')

    Login
      .fillForm(user)
      .sendForm();
    cy.location('pathname').should('eq', '/map/products');

    return Map;
  }
}
