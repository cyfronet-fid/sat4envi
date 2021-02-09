/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {UserOptionsSendView} from '../../page-objects/user-options/user-options-send-view-to-mail.po';
import {MapProducts} from '../../page-objects/map/map-products.po';
import {MapDateSelect} from '../../page-objects/map/map-date-select.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('products.json').as('products');
});

describe('Send View', () => {
  beforeEach(function () {
    cy.visit('/login');
    Login.loginAs(this.zkMember);
  });

  it('should send view to mail', function () {
    const year = 2020;
    const month = 2;
    const day = 1;

    cy.deleteAllMails();

    MapProducts.selectProductByName(this.products[3].name);
    MapDateSelect.openDateChange().selectDate(year, month, day);

    UserOptionsSendView.openSendViewsModal()
      .fillFields('test@mail.pl', 'caption-test', 'description-test')
      .sendView();

    MapProducts.selectProductByName(this.products[4].name);
    MapDateSelect.openDateChange().selectDate(year, month + 1, day + 1);

    UserOptionsSendView.clickShareView();

    MapProducts.selectedProductShouldHaveNameAndDate(
      this.products[3].name,
      year,
      month,
      day
    );
  });
});
