/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapDataSearch } from '../../page-objects/map/map-data-search.po';

before(function () {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Sentinel data search', () => {

  context("Data search for not logged in user", () => {

    before(() => {
      cy.visit('/login');
    });

    it('shouldn\'t download data without authentication', () => {
      Login
        .goToMap()
      MapDataSearch
        .goToSearchData()
        .selectNthProduct(0)
        .search()
        .downloadNthData(0)
        .shouldReturnToLoginPage()
    });
  });

  context("Data search for logged in user", () => {

    beforeEach(function(){
      cy.visit('/login');
      Login
        .loginAs(this.zkMember)
    });

    it("should search product data", () => {
      MapDataSearch
        .goToSearchData()
        .selectNthProduct(0)
        .search()
    });

    it("should display artifacts", () => {
      MapDataSearch
        .goToSearchData()
        .selectNthProduct(0)
        .search()
        .selectNthDataDetails(0)   
    })
  });
});