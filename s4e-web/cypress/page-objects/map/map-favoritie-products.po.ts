import {Core} from '../core.po';

export class MapFavorities extends Core {
  static readonly pageObject = {
    getNonFavoritesBtns: () => cy.get('.e2e-non-favourite-btn'),
    getFavoritesBtns: () => cy.get('.e2e-favourite-btn'),
    favoritesBtnClass: '.e2e-favourite-btn',
    getGoToFavouriteBtn: () => cy.get('[data-e2e="favourite-list-btn"]'),
    getGoToProductsBtn: () => cy.get('[data-e2e="product-list-btn"]'),
    getFavouriteCount: () => cy.get('[data-e2e="favourite-count"]'),
    getProductNavbar: () => cy.get('[data-e2e="product-list"]')
  };

  static favouritesCountShouldBe(count: number) {
    MapFavorities.pageObject
      .getFavouriteCount()
      .should('have.text', count.toString());

    return MapFavorities;
  }

  static unselectAllFavorites() {
    cy.server();
    cy.route('DELETE', '/api/v1/products/**/favourite').as('deleteFavourite');
    MapFavorities.pageObject.getFavoritesBtns().then($btns => {
      for (let i = 0; i < $btns.length; i++) {
        cy.get(MapFavorities.pageObject.favoritesBtnClass).eq(0).click();
        cy.wait('@deleteFavourite');
        cy.get('.item-fav-spinner').should('not.exist');
      }
    });

    return MapFavorities;
  }

  static selectNthAsFavorite(number: number) {
    cy.server();
    cy.route('PUT', '/api/v1/products/**/favourite').as('addFavourite');
    MapFavorities.pageObject.getNonFavoritesBtns().eq(number).click();
    cy.wait('@addFavourite');
    cy.get('.item-fav-spinner').should('not.exist');

    return MapFavorities;
  }

  static favouritesAreNotVisible() {
    MapFavorities.pageObject.getNonFavoritesBtns().should('have.length', 0);

    MapFavorities.pageObject.getFavoritesBtns().should('have.length', 0);

    return MapFavorities;
  }

  static goToFavourites() {
    MapFavorities.pageObject.getGoToFavouriteBtn().click();

    return MapFavorities;
  }

  static goToProducts() {
    MapFavorities.pageObject.getGoToProductsBtn().click();

    return MapFavorities;
  }

  static waitForProductsAndUnselectFavorites() {
    cy.wait(500)
      .get('body')
      .then(body => {
        if (body.find(MapFavorities.pageObject.favoritesBtnClass).length > 0) {
          MapFavorities.unselectAllFavorites();
        }
      });
  }
}
