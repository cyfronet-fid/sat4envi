import { Map } from '../map/map.po';

export interface User {
  email: string;
  password: string;
}

export namespace Login {
  export class PageObject {
    // TODO: update elements with data-e2e attributes
    static getLoginInput = () => cy.get('#login-login');
    static getPasswordInput = () => cy.get('#login-password');
    static getSubmitBtn = () => cy.get('button[type="submit"]');
  }

  export function loginAs(user: User) {
    cy.visit('/login');

    PageObject
      .getLoginInput()
      .should('be.visible')
      .type(user.email);
    PageObject
      .getPasswordInput()
      .should('be.visible')
      .type(user.password);
    PageObject
      .getSubmitBtn()
      .should('be.visible')
      .click();

    cy.location('pathname').should('eq', '/map/products');
    return Map;
  }
}
