/// <reference types="Cypress" />

context('Profile', () => {
  // IMPORTANT!!! This test can't be done due to bug with login and logout
  // TODO uncomment after reparation of up mentioned issue
  // it('Should change password', () => {
  //   // log in
  //   cy.visit('/login');
  //   cy.fixture('users/zkMember.json').as('zkMember')
  //     .then((zkMember) => {
  //       cy.get('#login-login').type(zkMember.email);
  //       cy.get('#login-password').type(zkMember.password);
  //       cy.get('button[type="submit"]').click();
  //     });

  //   // go to settings profile
  //   cy.location('pathname').should('eq', '/map/products');
  //   cy.get('#user-login-button').should('be.visible').click();
  //   cy.get('a').should('be.visible').contains('Ustawienia').click();

  //   cy.fixture('users/zkMember.json').as('zkMember')
  //     .then((zkMember) => {
  //       cy.get('#old-password').type(zkMember.password);
  //       cy.get('#new-password').type(zkMember.password.toUpperCase());
  //       cy.get('button[type="submit"]').click();
  //     });

  //   // logout
  //   cy.get('.login a').click();

  //   // log in with new password
  //   cy.location('pathname').should('eq', '/login');
  //   cy.fixture('users/zkMember.json').as('zkMember')
  //     .then((zkMember) => {
  //       cy.get('#login-login').type(zkMember.email);
  //       cy.get('#login-password').type(zkMember.password.toUpperCase());
  //       cy.get('button[type="submit"]').click();
  //     });

  //   // Reset password to default
  //   cy.location('pathname').should('eq', '/map/products');
  //   cy.get('#user-login-button').should('be.visible').click();
  //   cy.get('a').should('be.visible').contains('Ustawienia').click();

  //   cy.visit('/settings/profile');
  //   cy.fixture('users/zkMember.json').as('zkMember')
  //     .then((zkMember) => {
  //       cy.get('#old-password').type(zkMember.password.toUpperCase());
  //       cy.get('#new-password').type(zkMember.password);
  //       cy.get('button[type="submit"]').click();
  //     });
  // });
});
