import { Core } from '../core.po';

export class LocationsSearch extends Core {
  static pageObject = {
    getSearch: () => cy.get('[data-e2e="search-input"]'),
    getSearchResults: () => cy.get('[data-e2e="search-result"]'), //every city separetly
    getSearchResultsList: () => cy.get('[data-e2e="search-results-list"]'), // list of city
    getSearchResultsType: (searchResult: Cypress.Chainable<JQuery<HTMLElement>>) => searchResult.get('.type'),
    getSearchResultsVoivodeship: (searchResult: Cypress.Chainable<JQuery<HTMLElement>>) => searchResult.get('.voivodeship'),
    getClearSearchBtn: () => cy.get('[data-e2e="clear-search-btn"]'), // X buttom
    getSelectActiveResultBtn: () => cy.get('[data-e2e="select-active-result-btn"]') //serach icon
  };

  static type(value: string) {
    LocationsSearch
      .pageObject
      .getSearch()
      .clear()
      .type(value, { force: true });

    return LocationsSearch;
  }

  static nthResultShouldHaveLabel(nth: number, label: string) {
    LocationsSearch
      .pageObject
      .getSearchResults()
      .eq(nth)
      .contains(label)
      .should('be.visible');

    return LocationsSearch;
  }

  static nthResultShouldHaveType(nth: number, type: string) {
    LocationsSearch
      .pageObject
      .getSearchResultsType(
        LocationsSearch
        .pageObject
        .getSearchResults()
        .eq(nth)
        .should('be.visible')
      )
      .should('contain', type);

    return LocationsSearch;
  }

  static nthResultShouldHaveVoivodeship(nth: number, voivodeship: string) {
    LocationsSearch
      .pageObject
      .getSearchResultsVoivodeship(
        LocationsSearch
        .pageObject
        .getSearchResults()
        .eq(nth)
        .should('be.visible')
      )
      .should('contain', voivodeship);

    return LocationsSearch;
  }

  static selectNthResult(nth: number) {
    LocationsSearch
      .pageObject
      .getSearchResults()
      .eq(nth)
      .click({ force: true });

    return LocationsSearch;
  }

  static searchShouldHaveValue(value: string) {
    LocationsSearch
      .pageObject
      .getSearch()
      .should('be.visible')
      .should('have.value', value);

    return LocationsSearch;
  }

  static resultsShouldBeClosed() {
    LocationsSearch
      .pageObject
      .getSearchResultsList()
      .should('not.be.visible');

    return LocationsSearch;
  }

  static clearSearch() {
    LocationsSearch
      .pageObject
      .getClearSearchBtn()
      .click({ force: true });

    return LocationsSearch;
  }

  static selectActiveResult() {
    LocationsSearch
      .pageObject
      .getSelectActiveResultBtn()
      .click({ force: true });

    return LocationsSearch;
  }

  static waitForCitiesList() {
    cy.server()
    cy.route('GET', '/api/v1/places?namePrefix=warsz').as('getCityList');
    cy.wait('@getCityList').its('status').should('eq', 200)

    return LocationsSearch
  }
}
