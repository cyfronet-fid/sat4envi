import { Core } from '../core.po';

export class UserPasswordChange extends Core {
  static pageObject = {
    // TODO: update elements with data-e2e attributes
    getOldPasswordInput: () => cy.get('#oldPassword'),
    getNewPasswordInput: () => cy.get('#newPassword'),
    getSubmitBtn: () => cy.get('button[type="submit"]'),
  };

  static changePassword(oldPassword: string, newPassword: string) {
    UserPasswordChange
      .pageObject
      .getOldPasswordInput()
      .type(oldPassword);
      UserPasswordChange
      .pageObject
      .getNewPasswordInput()
      .type(newPassword);
      UserPasswordChange
      .pageObject
      .getSubmitBtn()
      .click();

    return UserPasswordChange;
  }
}

