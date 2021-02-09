/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {MapDateSelect} from '../../page-objects/map/map-date-select.po';
import {MapProducts} from '../../page-objects/map/map-products.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('products.json').as('products');
});

describe('Map Products', () => {
  beforeEach(function () {
    cy.server();

    cy.visit('/login');
    Login.loginAs(this.zkMember);
  });

  it('should load the product', function () {
    MapProducts.selectProductByName(this.products[3].name);
  });

  it('should the live scene be loading', function () {
    MapProducts.selectProductByName(this.products[5].name)
      .turnOnOnLiveView()
      .turnOffOnLiveView();
  });

  it('should load the selected date of the product', function () {
    const year = 2020;
    const month = 2;
    const day = 6;

    MapProducts.selectProductByName(this.products[0].name);
    MapDateSelect.openDateChange().selectDate(year, month, day);
  });

  it('should display legend', function () {
    MapProducts.selectProductByName(this.products[4].name).legendShouldBeVisible();
  });
});
