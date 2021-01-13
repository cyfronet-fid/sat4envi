
/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { JwtTokenModal } from "../../page-objects/user-options/user-options-get-token.po"

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');  
});

describe('Get JWT Token', () => {
  beforeEach(function () {
    cy.visit('/login')
    
    Login
    .loginAs(this.zkMember)
  });

  it('should get jwt token with good password', function () {
    
    JwtTokenModal
      .openJwtTokenModal()
      .authenticateAs(this.zkMember)
      .shouldHaveVisibleToken()
      .tokenShouldContain('.')
      .goToApiManual()
  })
});
