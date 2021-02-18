/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {Mobile} from '../../page-objects/mobile/mobile-products.po';
import {MapDateSelect} from '../../page-objects/map/map-date-select.po';
import {MapProducts} from '../../page-objects/map/map-products.po';

before(() => {
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('products.json').as('products');
});

describe('Mobile', () => {
  beforeEach(function () {
    cy.viewport(414, 846);
    cy.visit('/login');
    Login.loginAs(this.zkAdmin);
  });

  it('should load product and change date and hour', function () {
    const year = 2020;
    const month = 2;
    const day = 1;
    const hour = 2;

    Mobile.toggleDisplayProductsSidebar();
    MapProducts.selectProductByName(
      this.products[3].name
    ).closeDisplayProductDescription();
    Mobile.toggleDisplayProductsSidebar();
    MapDateSelect.openDateChange()
      .selectDate(year, month, day)
      .selectHourForMobile(hour);
  });

  it("shouldn't display timeline for change hour", function () {
    const year = 2025;
    const month = 2;
    const day = 1;

    Mobile.toggleDisplayProductsSidebar();
    MapProducts.selectProductByName(
      this.products[0].name
    ).closeDisplayProductDescription();
    Mobile.toggleDisplayProductsSidebar();
    MapDateSelect.openDateChange()
      .selectDate(year, month, day)
      .hoursSelectionLineShouldNotDisplayed()
      .hourSelectionShouldBeDisabledMobile();
  });
});
