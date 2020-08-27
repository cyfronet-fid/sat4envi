import promisify from 'cypress-promise';

export class Core {
  static callAndChangeContextTo<T>(any: any, page: T) {
    return Core.changeContextTo(page);
  }

  static changeContextTo<T>(page: T) {
    return page;
  }

  static goTo<T>(elementToClick: Cypress.Chainable<JQuery<HTMLElement>>, expectedUrl: string, context: T) {
    elementToClick
      .should('be.visible')
      .click({ force: true });

    cy.location('pathname').should('eq', expectedUrl);
    return context;
  }
}
