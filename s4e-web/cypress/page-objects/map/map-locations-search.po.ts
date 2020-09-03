import { Core } from '../core.po';

export class LocationsSearch extends Core {
  static pageObject = {
    getSearch: () => cy.get('[data-e2e="search-input"]'),
    getSearchResults: () => cy.get('[data-e2e="search-result"]'),
    getSearchResultsList: () => cy.get('[data-e2e="search-results-list"]'),
    getSearchResultsType: (searchResult: Cypress.Chainable<JQuery<HTMLElement>>) => searchResult.get('.type'),
    getSearchResultsVoivodeship: (searchResult: Cypress.Chainable<JQuery<HTMLElement>>) => searchResult.get('.voivodeship'),
    getClearSearchBtn: () => cy.get('[data-e2e="clear-search-btn"]'),
    getSelectActiveResultBtn: () => cy.get('[data-e2e="select-active-result-btn"]')
  };

  static type(value: string) {
    LocationsSearch
      .pageObject
      .getSearch()
      .should('be.visible')
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
      .should('be.visible')
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
      .should('be.visible')
      .click({ force: true });

    return LocationsSearch;
  }

  static selectActiveResult() {
    LocationsSearch
      .pageObject
      .getSelectActiveResultBtn()
      .should('be.visible')
      .click({ force: true });

    return LocationsSearch;
  }
}
