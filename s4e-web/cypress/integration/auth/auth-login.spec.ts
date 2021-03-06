/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {UserOptionsAuthentication} from '../../page-objects/user-options/user-option-authentication.po';
import {UserOptionsGoToSettings} from '../../page-objects/user-options/user-options-go-to-settings-profile.po';
import {SettingsUserPasswordChange} from '../../page-objects/settings/settings-change-password.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('users/admin.json').as('superAdmin');
  cy.fixture('users/userToRegister.json').as('userToRegister');
});

describe('Auth', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  context('Auth login', () => {
    it('should login as superAdmin', function () {
      Login.loginAs(this.superAdmin);
      UserOptionsAuthentication.logout();
    });

    it('should login as zkMember', function () {
      Login.loginAs(this.zkMember);
      UserOptionsAuthentication.logout();
    });

    it('should login as zkAdmin', function () {
      Login.loginAs(this.zkAdmin);
      UserOptionsAuthentication.logout();
    });

    it('should reset password and log in', function () {
      cy.deleteAllMails();

      Login.goToPasswordResetPage().enterEmailForResetPasswordLink(
        this.zkMember.email
      );

      Login.clickResetPasswordLink();

      Login.setNewPassword('12341234').loginAs({
        ...this.zkMember,
        password: '12341234'
      });
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsUserPasswordChange.goToChangePasswordPage().changePassword(
        '12341234',
        this.zkMember.password
      );
    });
  });

  context('Valid form', () => {
    it("shouldn't send empty form", function () {
      Login.unfillForm().errorsCountShouldBe(2);
    });

    it("shouldn't login not-registered user", function () {
      Login.fillForm(this.userToRegister).sendForm().hasErrorLogin();
    });

    it("should't login user when email have wrong format", function () {
      Login.fillForm({
        ...this.userToRegister,
        email: 'wrongFormat'
      }).errorsCountShouldBe(1);
    });

    it('should not allow login page for user with session', function () {
      Login.loginAs(this.superAdmin).loginPageShouldNotBeAllowed();
    });
  });
});
