/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {MapDataSearch} from '../../page-objects/map/map-data-search.po';
import {GeneralModal} from '../../page-objects/modal/general-modal.po';

before(function () {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Sentinel data search', () => {
  context('Data search for not logged in user', () => {
    before(() => {
      cy.visit('/login');
    });

    it("shouldn't download data without authentication", () => {
      Login.goToMap();
      MapDataSearch.goToSearchData()
        .selectNthProduct(0)
        .search()
        .downloadNthData(0)
        .shouldReturnToLoginPage();
    });
  });

  context('Data search for logged in user', () => {
    beforeEach(function () {
      cy.visit('/login');
      Login.loginAs(this.zkMember);
    });

    it('should search product data and display artifacts', () => {
      MapDataSearch.goToSearchData()
        .selectNthProduct(0)
        .search()
        .selectNthDataDetails(0);
      GeneralModal.closeModal();
      MapDataSearch.returnToSelectForm()
        .selectNthProduct(1)
        .search()
        .selectNthDataDetails(0);
      GeneralModal.closeModal();
      MapDataSearch.returnToSelectForm()
        .selectNthProduct(2)
        .search()
        .selectNthDataDetails(0);
      GeneralModal.closeModal();
    });
  });
});
