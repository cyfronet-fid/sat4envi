/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapDataSearch } from '../../page-objects/map/map-data-search.po';

before(function () {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Map Data Search', () => {

  context("Data search for not logged in user", () => {

    before(() => {
      cy.visit('/login');
    });

    it('shouldn\'t download data without authentication', () => {
      Login
        .goToMapWithoutLogin()
      MapDataSearch
        .goToSearchData()
        .selectNthProductToSearch(0)
        .searchData()
        .selectNthDataToDownload(0)
        .shouldReturnToLoginPage()
    });
  });

  context("Data search for logged in user", () => {

    beforeEach(function(){
      cy.visit('/login');
      Login
        .loginAs(this.zkMember)
    });

    it("should download data", () => {
      MapDataSearch
        .goToSearchData()
        .selectNthProductToSearch(0)
        .searchData()
        .selectNthDataToDownload(0);
    });

    it("should download artifacts", () => {
      MapDataSearch
        .goToSearchData()
        .selectNthProductToSearch(0)
        .searchData()
        .selectNthDataDetails(0)
        .downloadArtifacts();
    })
  });
});