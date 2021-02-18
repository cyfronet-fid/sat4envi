import {Core} from '../core.po';

export class UserOptionsGoToSettings extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOpenAdministrationPaneBtn: () => cy.get('[data-e2e="open-settings-btn"]'),
    getOpenUserProfileBtn: () => cy.get('[data-e2e="open-profile-btn"]'),
    getOptionsDropdown: () => cy.get('[data-e2e="options-dropdown"]')
  };

  static goToAdministrationPanel() {
    cy.location('href').should('include', '/map/products?');
    UserOptionsGoToSettings.pageObject.getOptionsBtn().click();

    UserOptionsGoToSettings.pageObject.getOpenAdministrationPaneBtn().click();

    cy.location('pathname').should('eq', '/settings/institutions');
  }

  static gotoUserProfile() {
    cy.location('href').should('include', '/map/products?');
    UserOptionsGoToSettings.pageObject.getOptionsBtn().click();

    UserOptionsGoToSettings.pageObject.getOpenUserProfileBtn().click();

    cy.location('pathname').should('eq', '/settings/profile');
  }
}
