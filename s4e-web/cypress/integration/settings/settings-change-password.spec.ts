/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/login.po';
import { SideNav } from '../../page-objects/settings/settings-side-nav.po';

context.skip('Settings change password', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  it('should change password and login with it', function () {
    Login
      .loginAs(this.zkMember)
      .goToSettingsAs(this.zkMember)
      .goToUserProfile()
      .goToPasswordChange()
      .changePassword(this.zkMember.password, this.zkMember.password.toUpperCase())
      .changeContextTo(SideNav)
      .logout();

    Login
      .loginAs({...this.zkMember, password: this.zkMember.password.toUpperCase()})
      .goToSettingsAs({...this.zkMember, password: this.zkMember.password.toUpperCase()})
      .goToUserProfile()
      .goToPasswordChange()
      .changePassword(this.zkMember.password.toUpperCase(), this.zkMember.password)
      .changeContextTo(SideNav)
      .logout();
  });
});
