/// <reference types="Cypress" />

import promisify from 'cypress-promise';
import { Login } from '../../page-objects/login/login.po';

context('Profile', () => {
  it('Should change password', async () => {
    const user = await promisify(cy.fixture('users/zkMember.json'));
    Login
      .loginAs(user)
      .goToSettingsAs(user)
      .goToProfile()
      .changePassword(user.password, user.password.toUpperCase())
      .getSideNav()
      .logout()

      .loginAs({...user, password: user.password.toUpperCase()})
      .goToSettingsAs({...user, password: user.password.toUpperCase()})
      .goToProfile()
      .changePassword(user.password.toUpperCase(), user.password)
      .getSideNav()
      .logout();
  });
});
