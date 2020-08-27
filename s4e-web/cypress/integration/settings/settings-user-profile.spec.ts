/// <reference types="Cypress" />

import promisify from 'cypress-promise';

import { Login } from '../../page-objects/login/login.po';
import { UserProfile } from '../../page-objects/settings/settings-user-profile.po';

context('Settings user profile', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login
      .loginAs(this.zkMember)
      .goToSettingsAs(this.zkMember)
      .goToUserProfile();
  });

  it('should display and navigate to change password', function () {
    UserProfile
      .userDetailsShouldContain(this.zkMember.email)
      .goToPasswordChange();
  });
});
