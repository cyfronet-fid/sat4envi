/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapFavorities } from '../../page-objects/map/map-favoritie-products.po';

before(function () {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Map favorite products', () => {

  context("Favorites for not logged in user", () => {

    before(() => {
      cy.visit('/login');
      MapFavorities
        .waitForProductsAndUnselectFavorites();
    });

    it('favourites shouldn\'t be visible without authentication', () => {
      Login.
        goToMap();
      MapFavorities
        .favouritesAreNotVisible();
    });
  });

  context("Favorities for logged in user", () => {

    beforeEach(function () {
      cy.visit('/login')
      Login
        .loginAs(this.zkMember)
    });

    it("Add and remove products from the favorities", () => {
      MapFavorities
        .goToFavourites()
        .favouritesCountShouldBe(0)
        .goToProducts()
        .selectNthAsFavorite(0)
        .favouritesCountShouldBe(1)
        .selectNthAsFavorite(3)
        .favouritesCountShouldBe(2)
        .unselectAllFavorites()
        .favouritesCountShouldBe(0)
    });
  });
});
