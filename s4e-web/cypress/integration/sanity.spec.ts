/// <reference types="Cypress" />

context('Sanity', () => {
  it('There should be login', () => {
    cy.visit('/login');
    cy.contains('Logowanie');
  });
});
