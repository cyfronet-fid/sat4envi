import { Core } from '../core.po';

export class UserOptionsGoToSettings extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOpenAdministrationPaneBtn: () => cy.get('[data-e2e="open-settings-btn"]'),
    getOpenUserProfileBtn: () => cy.get('[data-e2e="open-profile-btn"]')
  };

  static goToAdministrationPanel() {
    UserOptionsGoToSettings
      .pageObject
      .getOptionsBtn()
      .click();

    UserOptionsGoToSettings
      .pageObject
      .getOpenAdministrationPaneBtn()
      .click();

    cy.location("pathname").should("eq", "/settings/institutions");
  }

  static gotoUserProfile() {
    UserOptionsGoToSettings
      .pageObject
      .getOptionsBtn()
      .click();

    UserOptionsGoToSettings
      .pageObject
      .getOpenUserProfileBtn()
      .click();

    cy.location("pathname").should("eq", "/settings/profile");
  }
};