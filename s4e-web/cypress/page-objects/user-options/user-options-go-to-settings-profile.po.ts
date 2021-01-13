import { Core } from '../core.po';

export class GoToSettings extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOpenAdministrationPaneBtn: () => cy.get('[data-e2e="open-settings-btn"]'),
    getOpenUserProfileBtn: () => cy.get('[data-e2e="open-profile-btn"]')
  };

  static goToAdministrationPanel() {
    GoToSettings
      .pageObject
      .getOptionsBtn()
      .click();

    GoToSettings
      .pageObject
      .getOpenAdministrationPaneBtn()
      .click();

    cy.location("pathname").should("eq", "/settings/institutions");
  }

  static gotoUserProfile() {
    GoToSettings
      .pageObject
      .getOptionsBtn()
      .click();

    GoToSettings
      .pageObject
      .getOpenUserProfileBtn()
      .click();

    cy.location("pathname").should("eq", "/settings/profile");
  }
};