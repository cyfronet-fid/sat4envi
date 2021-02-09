/// <reference types="Cypress" />

import {environment} from '../../../src/environments/environment';

describe('ErrorHandling', () => {
  beforeEach(() => {
    cy.visit('/');
  });

  it('Should redirect to not found page', () => {
    cy.visit('/test');
    cy.url().should('contain', 'errors/404');

    cy.server({
      method: 'GET',
      delay: 500,
      status: 404,
      response: {}
    });
    cy.route(`${environment.apiPrefixV1}/products`, {
      errors: "Endpoint doesn't exists"
    });
    cy.visit('/');
    cy.url().should('contain', 'errors/404');
  });

  it('Should redirect to bad gateway page', () => {
    cy.server({
      method: 'GET',
      delay: 500,
      status: 502,
      response: {}
    });
    cy.route(`${environment.apiPrefixV1}/products`, {errors: 'Bad Gateway 502'});
    cy.visit('/');
    cy.url().should('contain', 'errors/502');
  });

  it('Should redirect to internal server error page', () => {
    cy.server({
      method: 'GET',
      delay: 500,
      status: 500,
      response: {}
    });
    cy.route(`${environment.apiPrefixV1}/products`, {
      errors: "Server don't responded"
    });
    cy.visit('/');
    cy.url().should('contain', 'errors/500');
  });

  it('Should logout and provide to login page', () => {
    cy.server({
      method: 'GET',
      delay: 500,
      status: 403,
      response: {}
    });
    cy.route(`${environment.apiPrefixV1}/products`, {errors: 'Access forbidden'});
    cy.visit('/');
    cy.url().should('contain', 'login');

    cy.server({
      method: 'GET',
      delay: 500,
      status: 401,
      response: {}
    });
    cy.route(`${environment.apiPrefixV1}/products`, {
      errors: "You're not authorized"
    });
    cy.visit('/');
    cy.url().should('contain', 'login');
  });
});
