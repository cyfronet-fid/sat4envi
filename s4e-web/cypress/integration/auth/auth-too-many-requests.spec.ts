/// <reference types="Cypress" />

import { environment } from '../../../src/environments/environment';
import promisify from 'cypress-promise';

context('Auth too many requests', () => {
  beforeEach(() => {
    cy.visit('/');
  });
  // TODO: Requests are not working correctly (are not sended)
  it('should propagate 429 error on more than 10 requests per minute', function () {
    const invalidUser = {
      email: 'invalid@mail.pl',
      password: 'test123'
    };
    const maxAttemptsPerMinute = 10;

    new Array(maxAttemptsPerMinute * 2)
      .forEach(async (requestNumber) => {
        const response = await promisify(cy.request('POST', `${environment}/login`, invalidUser));
        if (requestNumber > maxAttemptsPerMinute) {
          expect(response.status).to.be.eq(429);
        }
      });
  });
});
