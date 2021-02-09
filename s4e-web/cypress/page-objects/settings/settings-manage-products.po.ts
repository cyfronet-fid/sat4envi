import {Core} from '../core.po';

export class SettingsManageProducts extends Core {
  static readonly pageObject = {
    getManageProductsBtn: () => cy.get('[data-e2e="manage-products"]'),
    getInstitutionProducts: () => cy.get('[data-e2e="productName"]')
  };

  static goToManageProductsPage() {
    SettingsManageProducts.pageObject.getManageProductsBtn().click();

    cy.location('href').should('include', '/settings/manage-products');

    return SettingsManageProducts;
  }

  static productsCountShouldBe(count: number) {
    SettingsManageProducts.pageObject
      .getInstitutionProducts()
      .should('have.length', count);

    return SettingsManageProducts;
  }
}
