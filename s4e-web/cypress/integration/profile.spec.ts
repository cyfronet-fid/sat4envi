/// <reference types="Cypress" />

context('Profile', () => {
  // IMPORTANT!!! This test can't be done due to bug with login and logout
  // TODO uncomment after reparation of up mentioned issue
  it('Should change password', () => {
    // log in
    cy.visit('/login');
    cy.fixture('users/zkMember.json').as('zkMember')
      .then((zkMember) => {
        cy.get('#login-login').type(zkMember.email);
        cy.get('#login-password').type(zkMember.password);
        cy.get('button[type="submit"]').click();
      });
    cy.location('pathname').should('eq', '/map/products');

    // go to settings profile
    cy.get('#user-login-button').should('be.visible').click();
    cy.get('a').should('be.visible').contains('Ustawienia').click();
    cy.get('.navigation a').contains('Twój profil').should('be.visible').click();
    cy.location('pathname').should('eq', '/settings/profile');

    cy.fixture('users/zkMember.json').as('zkMember')
      .then((zkMember) => {
        cy.get('#old-password').type(zkMember.password);
        cy.get('#new-password').type(zkMember.password.toUpperCase());
        cy.get('button[type="submit"]').click();
        cy.wait(1000);
      });

    // logout
    cy.get('.login a').click();
    cy.location('pathname').should('eq', '/login');

    // log in with new password
    cy.fixture('users/zkMember.json').as('zkMember')
      .then((zkMember) => {
        cy.get('#login-login').type(zkMember.email);
        cy.get('#login-password').type(zkMember.password.toUpperCase());
        cy.get('button[type="submit"]').click();
        cy.wait(1000);
      });
    cy.location('pathname').should('eq', '/map/products');

    // Reset password to default
    cy.get('#user-login-button').should('be.visible').click();
    cy.get('a').should('be.visible').contains('Ustawienia').click();
    cy.get('.navigation a').contains('Twój profil').should('be.visible').click();
    cy.location('pathname').should('eq', '/settings/profile');

    cy.fixture('users/zkMember.json').as('zkMember')
      .then((zkMember) => {
        cy.get('#old-password').type(zkMember.password.toUpperCase());
        cy.get('#new-password').type(zkMember.password);
        cy.get('button[type="submit"]').click();
      });
  });
});
