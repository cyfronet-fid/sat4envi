/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapLocationsSearch } from '../../page-objects/map/map-locations-search.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Map Locations Search', () => {

  beforeEach(function () {
    cy.visit('/login')
    Login
      .loginAs(this.zkMember)
  });

  beforeEach(() => {
    MapLocationsSearch
      .pageObject
      .getSearch()
      .clear();
  });

  it('should display searched places', () => {
    MapLocationsSearch
      .searchCitiesBy('warsz')
      .nthResultShouldHaveLabel(8, 'Warszewo')
      .nthResultShouldHaveType(8, 'wieś')
      .nthResultShouldHaveVoivodeship(8, 'warmińsko-mazurskie')
      .selectNthResult(8)
      .searchShouldHaveValue('Warszewo')
      .resultsShouldBeClosed();
  });

  it('should clear input', () => {
    MapLocationsSearch
      .searchCitiesBy('szczeci')
      .nthResultShouldHaveLabel(0, 'Szczecin')
      .nthResultShouldHaveType(0, 'miasto')
      .nthResultShouldHaveVoivodeship(0, 'zachodniopomorskie')
      .clearSearch()
      .searchShouldHaveValue('');
  });

  it('should select active place on enter press', () => {
    MapLocationsSearch
      .searchCitiesBy('katow')
      .nthResultShouldHaveLabel(0, 'Katowice')
      .type('{enter}')
      .searchShouldHaveValue('Katowice');
  });

  it('should navigate with arrows', () => {
    MapLocationsSearch
      .searchCitiesBy('warsz')
      .nthResultShouldHaveLabel(1, 'Warszawiaki')
      .type('{downarrow}{enter}')
    MapLocationsSearch
      .searchShouldHaveValue('Warszawiaki');
  });
});

