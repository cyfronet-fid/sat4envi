/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { LocationsSearch } from '../../page-objects/map/map-locations-search.po';

describe('Map Locations Search', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    cy.visit('/login')
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


  it('should display searched places', () => {
    LocationsSearch
      .type('warsz')
      .waitForCitiesList('warsz')
      .nthResultShouldHaveLabel(0, 'Warszawa')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
      .selectNthResult(0)
      .searchShouldHaveValue('Warszawa')
      .resultsShouldBeClosed();
  });


  it('should clear input', () => {
    LocationsSearch
      .type('szczeci')
      .waitForCitiesList('szczeci')
      .nthResultShouldHaveLabel(0, 'Szczecin')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'zachodniopomorskie')
      .clearSearch()
      .searchShouldHaveValue('');
  });


  it('should select active place on loupe click', () => {
    LocationsSearch
      .type('warsz')
      .waitForCitiesList('warsz')
      .nthResultShouldHaveLabel(0, 'Warszawa')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'mazowieckie')
      .selectActiveResult()
      .searchShouldHaveValue('Warszawa');
  });

  it('should select active place on enter press', () => {
    LocationsSearch
      .type('katow')
      .waitForCitiesList('katow')
      .nthResultShouldHaveLabel(0, 'Katowice')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'śląskie')
      .type('{enter}')
      .searchShouldHaveValue('Katowice');
  });

  it('should navigate with arrows', () => {
    LocationsSearch
      .type('warsz')
      .waitForCitiesList('warsz')
      .nthResultShouldHaveLabel(1, 'Warszawiaki')
      .nthResultShouldHaveType(1, 'wieś')
      .nthResultShouldHaveVoivodeship(1, 'lubelskie')
      .type('{downarrow}{enter}')
      LocationsSearch
      .selectActiveResult()
      .searchShouldHaveValue('Warszawiaki');
  });
});



