/// <reference types="Cypress" />

import promisify from 'cypress-promise';
import { Map } from '../../page-objects/map/map.po';
import { Login } from '../../page-objects/auth/auth-login.po';
import { MapProducts } from '../../page-objects/map/map-products.po';
import { Core } from './../../page-objects/core.po';

describe.skip('Map favorite products', () => {
  beforeEach(function () {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login.loginAs(this.zkMember);

    cy.wait(500).get('body').then(body => {
      if (body.find(MapProducts.pageObject.favoritesBtnClass).length > 0) {
        MapProducts.unselectAllFavorites();
      }
    });
  });

  it('favourites shouldn\'t be visible without authentication', () => {
    Map
      .logout()
      .goTo(Login.pageObject.getGoToMapBtn, '/map/products', Map)
      .changeContextTo(MapProducts)
      .favouritesAreNotVisible();
  });
  it('Favourite tab should work', async () => {
    MapProducts
      .favouritesCountShouldBe(0)
      .selectFirstAsFavorite()
      .favouritesCountShouldBe(1);
    const selectedProductLabel = await promisify(
      MapProducts
        .pageObject
        .getProducts()
        .first()
        .invoke('text')
    );
    MapProducts
      .goToFavourites()
      .productsCountShouldBe(1)
      .callAndChangeContextTo(
        MapProducts
          .pageObject
          .getProducts()
          .first()
          .should('have.text', selectedProductLabel),
        MapProducts
      )
      .unselectAllFavorites()
      .favouritesShouldBeEmpty()
      .callAndChangeContextTo(cy.reload(), MapProducts)
      .isFavouriteActive();
  });
});
