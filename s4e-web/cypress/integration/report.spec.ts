/// <reference types="Cypress" />

// const downloadsFolder = require('downloads-folder');

context('zkMember', () => {
  beforeEach(() => {
    cy.visit('/login');
    cy.fixture('users/zkMember.json').as('zkMember').then((zkMember) => {
      cy.get('#login-login').type(zkMember.email, {force: true});
      cy.get('#login-password').type(zkMember.password, {force: true});
    });
    cy.get('button[type="submit"]').click({force: true});
  });

  it('should see ZK actions button', () => {
    cy.get('#zk-options-button').should('be.visible');
  });

  // this test will be disabled until https://github.com/cypress-io/cypress/issues/949 is resolved
  // it('should be able to download PDF report', () => {
  //   cy.get('#zk-options-button').click();
  //   cy.contains('Generuj Raport').click({force: true});
  //   cy.contains('.s4e-modal-header', 'Stwórz raport PDF', {timeout: 60000}).should('exist');
  //   cy.contains('Zapisz raport na dysk').should('be.disabled');
  //   cy.contains('Zapisz raport na dysk').should('not.be.disabled');
  //   const now = '1990-01-01T00:00:00.000Z';
  //
  //   cy.clock(new Date(now).getTime(), ['Date'] as any /* typing in cypress defs does not include Date, even though docs do */);
  //   cy.contains('Zapisz raport na dysk').click({force: true});
  //   cy.tick(1);
  //   cy.task('compareDownloadReport', now).should('be.true');
  //   cy.get('s4e-report-modal').should('not.exist');
  // });
});
