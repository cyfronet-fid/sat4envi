/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {SettingsNav} from '../../page-objects/settings/settings-navigation.po';
import {UserOptionsGoToSettings} from '../../page-objects/user-options/user-options-go-to-settings-profile.po';
import {SettingsUserPasswordChange} from '../../page-objects/settings/settings-change-password.po';

before(() => {
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
});

describe('Settings change password', () => {
  beforeEach(function () {
    cy.visit('/login');
    Login.loginAs(this.zkAdmin);
  });

  it('should change password and login with it', function () {
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsUserPasswordChange.goToChangePasswordPage().changePassword(
      this.zkAdmin.password,
      this.zkAdmin.password.toUpperCase()
    );
    SettingsNav.logOut();
    Login.fillForm(this.zkAdmin).sendForm().hasErrorLogin();
    Login.loginAs({...this.zkAdmin, password: this.zkAdmin.password.toUpperCase()});
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsUserPasswordChange.goToChangePasswordPage().changePassword(
      this.zkAdmin.password.toUpperCase(),
      this.zkAdmin.password
    );
  });
});
