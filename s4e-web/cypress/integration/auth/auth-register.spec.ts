/// <reference types = "Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {Registration} from '../../page-objects/auth/auth-register.po';
import {SettingsUserDeleteAccount} from '../../page-objects/settings/settings-delete-account.po';
import {UserOptionsGoToSettings} from '../../page-objects/user-options/user-options-go-to-settings-profile.po';
import {ConfirmModal} from '../../page-objects/modal/confirm-modal.po';

before(() => {
  cy.fixture('users/userToRegister.json').as('userToRegister');
});

describe('Register', () => {
  beforeEach(() => {
    cy.visit('/register');
  });

  context('Valid form', () => {
    it("shouldn't send empty form", function () {
      Registration.sendForm().errorsCountShouldBe(9);
    });

    it("shouldn't send form on incorrect email", function () {
      Registration.fillForm({...this.userToRegister, email: 'incorrect.pl'})
        .sendForm()
        .errorsCountShouldBe(1);
    });

    it("shouldn't send form on different passwords", function () {
      Registration.fillForm({
        ...this.userToRegister,
        repeatPassword: 'incorrectPassword'
      })
        .sendForm()
        .errorsCountShouldBe(1);
    });
  });

  context('Register user and delete account', () => {
    it('should register new user', function () {
      cy.deleteAllMails();

      Registration.registerAs(this.userToRegister);

      Registration.clickActivateLink();

      Login.loginAs(this.userToRegister);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsUserDeleteAccount.deleteAccount(this.userToRegister.password);
      ConfirmModal.accept();
      Login.fillForm(this.userToRegister).sendForm().hasErrorLogin();
    });
  });
});
