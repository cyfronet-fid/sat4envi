/// <reference types="Cypress" />

import { Login } from '../../page-objects/login/login.po';
import { LocationsSearch } from '../../page-objects/map/map-locations-search.po';

context('Map Locations Search', () => {
  before(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  before(function () {
    Login
      .loginAs(this.zkMember)
      .changeContextTo(LocationsSearch);
  });

  beforeEach(() => {
    LocationsSearch
      .pageObject
      .getSearch()
      .should('be.visible')
      .clear();
  });

  it('should display searched places', () => {
    LocationsSearch
      .type('warsz')
      .callAndChangeContextTo(
        cy
          .wait(700)
          .get('.searchResults', {timeout: 10000})
          .should('be.visible'),
        LocationsSearch
      )
      .nthResultShouldHaveLabel(0, 'Warszawa')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
      .selectNthResult(0)
      .searchShouldHaveValue('Warszawa')
      .resultsShouldBeClosed();
  });
  // TODO: fix and refactor
  // after search and selection of this same city search isn't filled with chosen city name
  // it('should clear input', () => {
  //   LocationsSearch
  //     .type('warsz')
  //     .callAndChangeContextTo(
  //       cy
  //         .wait(700)
  //         .get('.searchResults', {timeout: 10000})
  //         .should('be.visible'),
  //       LocationsSearch
  //     )
  //     .nthResultShouldHaveLabel(0, 'Warszawa')
  //     .nthResultShouldHaveType(0, 'miasto')
  //     .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
  //     .clearSearch()
  //     .searchShouldHaveValue('');
  // });
  // it('should select active place on loupe click', () => {
  //   LocationsSearch
  //     .type('warsz')
  //     .callAndChangeContextTo(
  //       cy
  //         .wait(700)
  //         .get('.searchResults', {timeout: 10000})
  //         .should('be.visible'),
  //       LocationsSearch
  //     )
  //     .nthResultShouldHaveLabel(0, 'Warszawa')
  //     .nthResultShouldHaveType(0, 'miasto')
  //     .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
  //     .selectActiveResult()
  //     .searchShouldHaveValue('Warszawa');
  // });
  // it('should select active place on enter press', () => {
  //   LocationsSearch
  //     .type('warsz')
  //     .callAndChangeContextTo(
  //       cy
  //         .wait(700)
  //         .get('.searchResults', {timeout: 10000})
  //         .should('be.visible'),
  //       LocationsSearch
  //     )
  //     .nthResultShouldHaveLabel(0, 'Warszawa')
  //     .nthResultShouldHaveType(0, 'miasto')
  //     .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
  //     .type('{enter}')
  //     .searchShouldHaveValue('Warszawa');
  // });
  // it('should navigate with arrows', () => {
  //   LocationsSearch
  //     .type('warsz')
  //     .callAndChangeContextTo(
  //       cy
  //         .wait(700)
  //         .get('.searchResults', {timeout: 10000})
  //         .should('be.visible'),
  //       LocationsSearch
  //     )
  //     .nthResultShouldHaveLabel(1, 'Warszawiaki')
  //     .nthResultShouldHaveType(1, 'wieś')
  //     .nthResultShouldHaveVoivodeship(1, 'lubelskie')
  //     .type('{downarrow}')
  //     .selectActiveResult()
  //     .searchShouldHaveValue('Warszawiaki');
  // });
});



