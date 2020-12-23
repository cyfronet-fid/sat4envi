/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { GoToSettings } from "../../page-objects/user-options/user-options-go-to-settings-profile.po"

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Settings Profile', () => {
  beforeEach(function () {
    cy.visit('/login')

    Login
      .loginAs(this.zkMember)
  });
  // skip due to a bug in the application
  it.skip('should redirect to administration Pane', function () {
    GoToSettings
      .goToAdministrationPanel();
  });

  it('should redirect to user profile', function () {
    GoToSettings
      .gotoUserProfile();
  });
});
