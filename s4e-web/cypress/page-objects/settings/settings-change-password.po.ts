import { Core } from '../core.po';

export class SettingsUserPasswordChange extends Core {
  static readonly pageObject = {
    getOldPasswordInput: () => cy.get('[data-e2e="oldPassword"]'),
    getNewPasswordInput: () => cy.get('[data-e2e="newPassword"]'),
    getSubmitBtn: () => cy.get('[data-e2e="btn-submit"]'),
    getChangePasswordBtn: () => cy.get('[data-e2e="go-to-password-change"]')
  };

  static goToChangePasswordPage() {
    SettingsUserPasswordChange
      .pageObject
      .getChangePasswordBtn()
      .click()

    cy.location('pathname').should('eq', '/settings/change-password');

    return SettingsUserPasswordChange;
  }

  static changePassword(oldPassword: string, newPassword: string) {
    cy.server()
    cy.route('POST', '/api/v1/password-change').as('passwordChange')
    SettingsUserPasswordChange
      .pageObject
      .getOldPasswordInput()
      .type(oldPassword);
    SettingsUserPasswordChange
      .pageObject
      .getNewPasswordInput()
      .type(newPassword);
    SettingsUserPasswordChange
      .pageObject
      .getSubmitBtn()
      .click();

    cy.wait('@passwordChange')

    return SettingsUserPasswordChange;
  }
};

