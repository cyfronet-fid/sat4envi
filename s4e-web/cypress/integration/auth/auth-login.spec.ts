/// <reference types="Cypress" />

import { Login } from '../../page-objects/login/login.po';

context('Auth login', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
    cy.fixture('users/zkAdmin.json').as('zkAdmin');
    cy.fixture('users/admin.json').as('superAdmin');
  });

  beforeEach(() => {
    cy.visit('/');
  });

  it('should login as zkMember', function () {
    Login
      .loginAs(this.zkMember)
      .logout();
  });
  it('should login as zkAdmin', function () {
    Login
      .loginAs(this.zkAdmin)
      .logout();
  });
  it('should login as superAdmin', function () {
    Login
      .loginAs(this.superAdmin)
      .logout();
  });
});
