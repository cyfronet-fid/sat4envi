/// <reference types="Cypress" />

context('Sanity', () => {
  beforeEach(() => {
    cy.visit('/')
  });

  it('There should be Products control', () => {
    cy.contains('Produkty')
  });
});
