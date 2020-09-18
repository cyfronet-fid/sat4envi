import { Core } from '../core.po';

export class Breadcrumbs extends Core {
  static pageObject = {
    getItems: () => cy.get('[data-e2e="breadcrumb-item"]')
  };

  static shouldHaveTexts(...texts: string[]) {
    Breadcrumbs
      .pageObject
      .getItems()
      .should('be.visible');

    texts
      .forEach(text => {
        Breadcrumbs
          .pageObject
          .getItems()
          .should('contain', text);
      });

    return Breadcrumbs;
  }

  static goToBreadcrumbWithLabel<T>(label: string, path: string, context: T) {
    Breadcrumbs
      .pageObject
      .getItems()
      .contains(label)
      .click({ force: true });

    cy.location('pathname').should('eq', path);

    return context;
  }
}
