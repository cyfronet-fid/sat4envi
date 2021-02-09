import {Core} from '../core.po';

export class MapProducts extends Core {
  static readonly pageObject = {
    getAllProductsInAllCategories: () =>
      cy.get('[data-e2e="product-list"] [data-e2e="products__item"]'),
    getProductNavbar: () => cy.get('[data-e2e="product-list"]'),
    getProductsNameBtn: () =>
      cy.get('[data-e2e="product-list"] [data-e2e="picker-item-label"]'),
    getProductsNameClass: '[data-e2e="picker-item-label"]',
    getLegend: () => cy.get('[data-e2e="legend__chart"]'),
    getOnLiveBtn: () => cy.get('[data-e2e="btn-live"]'),
    getSpinnerIcon: () => cy.get('.products__visibility__spinner'),
    getSelectedProductIcon: () => cy.get('button[data-e2e="selected-icon"]'),
    getOpenProductDescriptionBtn: () =>
      cy.get('[data-e2e="product-description-button"]'),
    getCloseProductDescriptionBtn: () =>
      cy.get('[data-e2e="close-product-description-btn"]')
  };

  static selectProductByName(partialName: string) {
    cy.server();
    cy.route('GET', '/api/v1/products/*').as('turnOnProduct');

    MapProducts.pageObject
      .getProductsNameBtn()
      .contains(partialName)
      .should('be.visible')
      .click();

    cy.wait('@turnOnProduct');

    MapProducts.pageObject.getSpinnerIcon().should('not.exist');

    MapProducts.pageObject
      .getProductsNameBtn()
      .contains(partialName)
      .should('have.class', 'active');

    return MapProducts;
  }

  static selectNthProduct(number: number) {
    MapProducts.pageObject
      .getProductsNameBtn()
      .eq(number)
      .should('be.visible')
      .click();

    return MapProducts;
  }

  static productsCountShouldBe(count: number) {
    MapProducts.pageObject
      .getAllProductsInAllCategories()
      .should('have.length', count);

    return MapProducts;
  }

  static legendShouldBeVisible() {
    MapProducts.pageObject.getLegend().should('be.visible');

    return MapProducts;
  }

  static turnOnOnLiveView() {
    cy.server();
    cy.route('GET', '/api/v1/products/*/scenes/most-recent?{*,*/*}').as(
      'loadResentScene'
    );

    MapProducts.pageObject
      .getOnLiveBtn()
      .click()
      .find('label')
      .should('have.class', 'active');
    cy.wait('@loadResentScene');

    return MapProducts;
  }

  static turnOffOnLiveView() {
    MapProducts.pageObject
      .getOnLiveBtn()
      .click()
      .find('label')
      .should('not.have.class', 'active');

    return MapProducts;
  }

  static productWithNameShouldNotBeVisible(partialName: string) {
    MapProducts.pageObject
      .getProductsNameBtn()
      .contains(partialName)
      .should('not.exist');

    return MapProducts;
  }

  static selectedProductShouldHaveNameAndDate(
    name: string,
    year: number,
    month: number,
    day: number
  ) {
    cy.location('href')
      .should('include', '/map/products?')
      .should(
        'include',
        `date=${year}-${month < 10 ? '0' + month : month}-${
          day < 10 ? '0' + day : day
        }`
      );

    MapProducts.pageObject.getAllProductsInAllCategories().should('be.visible');
    MapProducts.pageObject.getSpinnerIcon().should('not.exist');

    MapProducts.pageObject
      .getProductsNameBtn()
      .contains(name)
      .should('have.class', 'active');
  }

  static openDisplayProductDescription() {
    MapProducts.pageObject.getOpenProductDescriptionBtn().click();
  }

  static closeDisplayProductDescription() {
    MapProducts.pageObject.getCloseProductDescriptionBtn().click();
  }
}
