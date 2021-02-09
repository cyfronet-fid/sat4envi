import {Core} from '../core.po';

export class UserOptionsAuthentication extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getLoginOutBtn: () => cy.get('[data-e2e="logout-btn"]'),
    getOptionsDropdown: () => cy.get('[data-e2e="options-dropdown"]')
  };
  static logout() {
    UserOptionsAuthentication.pageObject
      .getOptionsBtn()
      .should('be.visible')
      .click();

    UserOptionsAuthentication.pageObject.getOptionsDropdown().should('be.visible');

    UserOptionsAuthentication.pageObject.getLoginOutBtn().click();

    cy.location('pathname').should('eq', '/login');

    return UserOptionsAuthentication;
  }
}
