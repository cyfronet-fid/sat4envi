import { Core } from './../core.po';

export class MapProducts extends Core {
  static pageObject = {
    getNonFavoritesBtns: () => cy.get(".e2e-non-favourite-btn"), //start icon
    favoritesBtnClass: ".e2e-favourite-btn",
    getFavoritesBtns: () => cy.get(MapProducts.pageObject.favoritesBtnClass),
    getFavouriteTab: () => cy.get('[data-e2e="favourite-list"]'), // link favorite
    getProductsTab: () => cy.get('[data-e2e="product-list"]'), // link list of products
    getFavouriteCount: () => cy.get('[data-e2e="favourite-count"]'), // count of favorite
    getProducts: () => cy.get(".products-list .products__item"), // all procucts 
    getProductList: () => cy.get(".products-list"), // list od prodcts (one element)
    getProductsBtns: () =>
      cy.get('s4e-items-picker.section.products .products__name'), // button for select every product
  }

  static selectProductBy(partialName: string) {
    MapProducts
      .pageObject
      .getProductsBtns()
      .contains(partialName)
      .should("be.visible")
      .click({ force: true });
    return MapProducts;
  }

  static selectNthProduct(number: number) {
    MapProducts
      .pageObject.getProductsBtns()
      .eq(number)
      .should("be.visible")
      .click({ force: true });
    return MapProducts;
  }

  static productsCountShouldBe(count: number) {
    MapProducts
      .pageObject
      .getProducts()
      .should('have.text', count.toString());

    return MapProducts;
  }

  static favouritesShouldBeEmpty() {
    MapProducts
      .pageObject
      .getProductList()
      .should('contain', 'Nie posiadasz ulubionych produktów');

    return MapProducts;
  }

  static favouritesCountShouldBe(count: number) {
    MapProducts
      .pageObject
      .getFavouriteCount()
      .should('have.text', count.toString());

    return MapProducts;
  }

  static selectAllFavorites() {
    MapProducts
      .pageObject.getNonFavoritesBtns()
      .click({ multiple: true, force: true });

    return MapProducts;
  }

  static unselectAllFavorites() {
    MapProducts
      .pageObject.getFavoritesBtns()
      .click({ multiple: true, force: true });

    return MapProducts;
  }

  static favouritesAreNotVisible() {
    MapProducts
      .pageObject
      .getNonFavoritesBtns()
      .should('have.length', 0);

    MapProducts
      .pageObject
      .getFavoritesBtns()
      .should('have.length', 0);

    return MapProducts;
  }

  static selectFirstAsFavorite() {
    MapProducts
      .pageObject
      .getNonFavoritesBtns()
      .first()
      .should("exist")
      .click({ force: true });

    return MapProducts;
  }

  static goToFavourites() {
    MapProducts
      .pageObject
      .getFavouriteTab()
      .click({ force: true });
    return MapProducts;
  }

  static goToProducts() {
    MapProducts
      .pageObject
      .getProductsTab()
      .click({ force: true });
    return MapProducts;
  }

  static isFavouriteActive() {
    MapProducts
      .pageObject
      .getFavouriteTab().should("have.class", "active");
    return MapProducts;
  }
}
