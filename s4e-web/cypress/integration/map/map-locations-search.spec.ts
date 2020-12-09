/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { LocationsSearch } from '../../page-objects/map/map-locations-search.po';

describe.skip('Map Locations Search', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login
      .loginAs(this.zkMember)
      .changeContextTo(LocationsSearch);
  });

  beforeEach(() => {
    LocationsSearch
      .pageObject
      .getSearch()
      .clear();
  });

  function waitForCitiesList() {
    cy.server()
    cy.route('GET', '/api/v1/places?namePrefix=warsz').as('getCityList');
    cy.wait('@getCityList').its('status').should('eq', 200)
  }

  it('should display searched places', () => {
    LocationsSearch
      .type('warsz')
    waitForCitiesList()
    LocationsSearch
      .nthResultShouldHaveLabel(0, 'Warszawa')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
      .selectNthResult(0)
      .searchShouldHaveValue('Warszawa')
      .resultsShouldBeClosed();
  });


  it('should clear input', () => {
    LocationsSearch
      .type('warsz')
    waitForCitiesList()
    LocationsSearch
      .nthResultShouldHaveLabel(0, 'Warszawa')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
      .clearSearch()
      .searchShouldHaveValue('');
  });


  it('should select active place on loupe click', () => {
    LocationsSearch
      .type('warsz')
    waitForCitiesList()
    LocationsSearch
      .nthResultShouldHaveLabel(0, 'Warszawa')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
      .selectActiveResult()
      .searchShouldHaveValue('Warszawa');
  });

  it('should select active place on enter press', () => {
    LocationsSearch
      .type('warsz')
    waitForCitiesList()
    LocationsSearch
      .nthResultShouldHaveLabel(0, 'Warszawa')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
      .type('{enter}')
      .searchShouldHaveValue('Warszawa');
  });

  it('should navigate with arrows', () => {
    LocationsSearch
      .type('warsz')
    waitForCitiesList()
    LocationsSearch
      .nthResultShouldHaveLabel(1, 'Warszawiaki')
      .nthResultShouldHaveType(1, 'wieś')
      .nthResultShouldHaveVoivodeship(1, 'lubelskie')
      .type('{downarrow}{enter}')
      .selectActiveResult()
      .searchShouldHaveValue('Warszawiaki');
  });
});



