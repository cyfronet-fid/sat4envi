/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { UserOptionsSendView } from "../../page-objects/user-options/user-options-send-view-to-mail.po";
import { MapProducts } from "../../page-objects/map/map-products.po"


before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('products.json').as('products'); 
});

describe('Send View', () => {
  beforeEach(function () {
    cy.visit("/login")
    Login.loginAs(this.zkMember);
  });

  it('should send view to mail', function(){
    cy.deleteAllMails();

    MapProducts
     .selectProductByName(this.products[3].name)

    UserOptionsSendView
      .openSendViewsModal()
      .fillFields("test@mail.pl", "caption-test", "description-test")
      .sendView()

   cy.clickShareView()
  });
});

