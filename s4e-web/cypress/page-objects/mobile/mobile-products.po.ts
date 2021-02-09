import {Core} from '../core.po';

export class Mobile extends Core {
  static readonly pageObject = {
    getToggleMobileSidebarDisplayBtn: () =>
      cy.get('[data-e2e="mobile-dropdown-products-button"]')
  };

  static toggleDisplayProductsSidebar() {
    Mobile.pageObject.getToggleMobileSidebarDisplayBtn().click();
  }
}
