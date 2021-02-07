import { Core } from '../core.po';

export class SettingsUserDeleteAccount extends Core {
  static readonly pageObject = {
    getPasswordInput: () => cy.get('[data-e2e="password"]').find('input'),
    getSubmitBtn: () => cy.get('[data-e2e="submit-btn"]'),
  };

  static deleteAccount(password: string) {
    SettingsUserDeleteAccount
      .pageObject
      .getPasswordInput()
      .type(password)

    SettingsUserDeleteAccount
      .pageObject
      .getSubmitBtn()
      .click()

    return SettingsUserDeleteAccount;
  }
};

