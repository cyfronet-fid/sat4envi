import { Core } from '../core.po';

export class MapProducts extends Core {

  static readonly pageObject = {
    getAllProductsInAllCategories: () => cy.get('[data-e2e="product-list"] [data-e2e="products__item"]'),
    getProductNavbar: () => cy.get('[data-e2e="product-list"]'),
    getProductsNameBtn: () => cy.get('[data-e2e="product-list"] [data-e2e="picker-item-label"]'),
    getLegend: () => cy.get('[data-e2e="legend__chart"]'),
    getOnLiveBtn: () => cy.get('[data-e2e="btn-live"]'),
  };

  static selectProductByName(partialName: string) {
    cy.server();
    cy.route('GET', '/api/v1/products/*').as('turnOnProduct')

    MapProducts
      .pageObject
      .getProductsNameBtn()
      .contains(partialName)
      .should("be.visible")
      .click();

    cy.wait('@turnOnProduct');
    cy.get('.products__visibility__spinner').should('not.exist');

    MapProducts
      .pageObject
      .getProductsNameBtn()
      .contains(partialName)
      .should("have.class", "active");

    return MapProducts;
  };

  static selectNthProduct(number: number) {
    MapProducts
      .pageObject
      .getProductsNameBtn()
      .eq(number)
      .should("be.visible")
      .click();
    return MapProducts;
  };

  static productsCountShouldBe(count: number) {
    MapProducts
      .pageObject
      .getAllProductsInAllCategories()
      .should('have.length', count);

    return MapProducts;
  };

  static legendShouldBeVisible() {
    MapProducts
      .pageObject
      .getLegend()
      .should("be.visible");

    return MapProducts;
  };

  static turnOnOnLiveView() {
    cy.route('GET', '/api/v1/products/*/scenes/most-recent?timeZone=Europe/*').as('loadResentScene')
    MapProducts
      .pageObject
      .getOnLiveBtn()
      .click()
      .find("label")
      .should("have.class", "active")
    cy.wait('@loadResentScene');

    return MapProducts;
  };

  static turnOffOnLiveView() {
    MapProducts
      .pageObject
      .getOnLiveBtn()
      .click()
      .find("label")
      .should("not.have.class", "active");

    return MapProducts;
  };
};