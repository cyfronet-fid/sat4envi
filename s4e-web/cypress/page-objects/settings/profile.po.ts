import { SideNav } from './side-nav.po';

export namespace Profile {
  export class PageObject {
    // TODO: update elements with data-e2e attributes
    static getOldPasswordInput = () => cy.get('#old-password');
    static getNewPasswordInput = () => cy.get('#new-password');
    static getSubmitBtn = () => cy.get('button[type="submit"]');
  }

  export function changePassword(oldPassword: string, newPassword: string) {
    PageObject
      .getOldPasswordInput()
      .type(oldPassword);
    PageObject
      .getNewPasswordInput()
      .type(newPassword);
    PageObject
      .getSubmitBtn()
      .click();

    return Profile;
  }

  export function getSideNav() {
    return SideNav;
  }
}

