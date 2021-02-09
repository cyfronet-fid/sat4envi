/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {UserOptionsGoToSettings} from '../../page-objects/user-options/user-options-go-to-settings-profile.po';

before(() => {
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
});

describe('Settings Profile', () => {
  beforeEach(function () {
    cy.visit('/login');

    Login.loginAs(this.zkAdmin);
  });

  it('should redirect to administration Pane', function () {
    UserOptionsGoToSettings.goToAdministrationPanel();
  });

  it('should redirect to user profile', function () {
    UserOptionsGoToSettings.gotoUserProfile();
  });
});
