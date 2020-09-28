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
    getGoToMapBtn: () => cy.get('a[data-test="go-to-map-btn"]')
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

  static sendForm() {
    Login
      .pageObject
      .getSubmitBtn()
      .click();

    cy.location('pathname').should('eq', '/map/products');
    return Login;
  }

  static loginAs(user: User) {
    // force logout
    cy.clearLocalStorage('s4eStore');

    cy.visit('/login');
    Login
      .fillForm(user)
      .sendForm();

    return Map;
  }
}
