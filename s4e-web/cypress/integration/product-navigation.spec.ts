/// <reference types="Cypress" />
import promisify from 'cypress-promise';
import { Login } from "../page-objects/login/login.po";
import { Map } from "../page-objects/map/map.po";

context('productNavigation', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('user');
  });

  beforeEach(() => {
    cy.visit('/');
  });

  it('should load product map', function () {
    const year = 2018;
    const month = 10;
    const day = 4;
    const hour = 2;
    Login
      .loginAs(this.user)
      .selectProductBy('108m')
      .openDateChange()
      .selectDate(year, month, day, hour);

    // this is a very fragile hack, we basically give 5 seconds for the request and then check it
    // cypress by itself can not intercept wms (image) requests, so thats the only way it can be
    // resonably tested at all.
    cy.wait(7500).window().then(win => {
      const networkrequests = win.performance.getEntries()
        .filter(r => /http:\/\/.+\/wms\?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&LAYERS=development%3A108m&TIME=2018-10-04T00%3A00%3A00.000Z&CRS=EPSG%3A3857&.+/.test(r.name));
    });
  });

  it('should search places', () => {
    cy.get('.search__input').type('warsz');
    cy.get('.searchResults', {timeout: 10000}).should('be.visible');
    cy.get('.searchresult__container > ul > :nth-child(1)').as('searchResult');
    cy.get('@searchResult').should('contain', 'Warszawa');
    cy.get('@searchResult').get('.type').should('contain', 'miasto');
    cy.get('@searchResult').get('.voivodeship').should('contain', 'mazowieckie');
    cy.get('@searchResult').click();
    cy.get('.search__input').should('have.value', 'Warszawa');
    cy.get('.searchResults').should('not.visible');
    cy.get('.reset_search_button').click();
    cy.get('.search__input').should('have.value', '');
  });

  describe('as logged in user', () => {
    beforeEach(function () {
      cy.fixture('users/zkMember.json').as('user');
    });

    beforeEach(function () {
      Login
        .loginAs(this.user)
    });

    it('should set favourite after login', () => {
      Map.selectAllFavorites()
      cy.visit('/');
      Map.unselectAllFavorites();
    });

    it('favourites tab should work', async () => {
      Map.PageObject.getFavouriteCount().should('have.text', '0');
      Map.selectFirstAsFavorite()
      const firstProductText = await promisify(Map.PageObject.getProducts().first().invoke('text'));

      Map.PageObject.getFavouriteCount().should('have.text', '1');
      Map.goToFavourites()

      Map.PageObject.getProducts().should('have.length', 1);

      Map.PageObject.getProducts().first().should('have.text', firstProductText);

      Map.unselectAllFavorites();

      Map.PageObject.getProductList().should('contain', 'Nie posiadasz ulubionych produktów');

      cy.reload();

      Map.isFavouriteActive();
    });
  });
});
