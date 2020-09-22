import { Map } from '../map/map.po';
import { Core } from '../core.po';

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
      .should('be.visible')
      .type(user.email);
    Login
      .pageObject
      .getPasswordInput()
      .should('be.visible')
      .type(user.password);

    return Login;
  }

  static sendForm() {
    Login
      .pageObject
      .getSubmitBtn()
      .should('be.visible')
      .click();

    return Login;
  }

  static loginAs(user: User) {
    cy.clearLocalStorage('s4eStore');

    return Login
      .callAndChangeContextTo(cy.visit('/login'), Login)
      .fillForm(user)
      .sendForm()
      .callAndChangeContextTo(cy.location('pathname').should('eq', '/map/products'), Map);
  }
}
