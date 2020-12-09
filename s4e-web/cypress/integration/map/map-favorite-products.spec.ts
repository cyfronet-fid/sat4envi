/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapFavorities } from '../../page-objects/map/map-favorities.po';
import {MapProducts} from '../../page-objects/map/map-products.po';

describe('Map favorite products', () => {

  context("Favorites for not logged in user", () => {

    before(() => {
      cy.visit('/login')
      MapFavorities
      .waitForProductsAndUnselectFavorites()
    })

    it('favourites shouldn\'t be visible without authentication', () => {
      Login.
        goToMapWithoutLogin()
      MapFavorities
        .favouritesAreNotVisible();
    })
  });


  context("Favorities for logged in user", () => {

    beforeEach(function () {
      cy.fixture('users/zkMember.json').as('zkMember');
    });

    beforeEach(function () {
      cy.visit('/login')
      Login
        .loginAs(this.zkMember)
    })


    it.only("Add and remove products from the favorities", () => {

      MapFavorities
        .goToFavourites()
        .favouritesShouldBeEmpty()
        .goToProducts()
        .selectNthAsFavorite(0)
        .favouritesCountShouldBe(1)
        .selectNthAsFavorite(3)
        .favouritesCountShouldBe(2)
      MapFavorities
        .unselectAllFavorites()
        .favouritesCountShouldBe(0)
        .favouritesShouldBeEmpty()
    })
  })

});
