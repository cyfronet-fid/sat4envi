import {Core} from '../core.po';

export class MapLocationsSearch extends Core {
  static readonly pageObject = {
    getSearch: () => cy.get('[data-e2e="search-input"]'),
    getSearchResults: () => cy.get('[data-e2e="search-result"]'),
    getSearchResultsList: () => cy.get('[data-e2e="search-results-list"]'),
    getSearchResultsType: (searchResult: Cypress.Chainable<JQuery<HTMLElement>>) =>
      searchResult.get('.type'),
    getSearchResultsVoivodeship: (
      searchResult: Cypress.Chainable<JQuery<HTMLElement>>
    ) => searchResult.get('.voivodeship'),
    getClearSearchBtn: () => cy.get('[data-e2e="clear-search-btn"]'),
    getSelectActiveResultBtn: () => cy.get('[data-e2e="select-active-result-btn"]')
  };

  static type(value: string) {
    MapLocationsSearch.pageObject.getSearch().type(value);

    return MapLocationsSearch;
  }

  static searchCitiesBy(name: string) {
    cy.server();
    cy.route('GET', `/api/v1/places?namePrefix=${name}`).as('getCityList');

    MapLocationsSearch.pageObject.getSearch().type(name);

    cy.wait('@getCityList');

    return MapLocationsSearch;
  }

  static nthResultShouldHaveLabel(nth: number, label: string) {
    MapLocationsSearch.pageObject
      .getSearchResults()
      .eq(nth)
      .contains(label)
      .should('be.visible');

    return MapLocationsSearch;
  }

  static nthResultShouldHaveType(nth: number, type: string) {
    MapLocationsSearch.pageObject
      .getSearchResultsType(
        MapLocationsSearch.pageObject.getSearchResults().eq(nth).should('be.visible')
      )
      .should('contain', type);

    return MapLocationsSearch;
  }

  static nthResultShouldHaveVoivodeship(nth: number, voivodeship: string) {
    MapLocationsSearch.pageObject
      .getSearchResultsVoivodeship(
        MapLocationsSearch.pageObject.getSearchResults().eq(nth).should('be.visible')
      )
      .should('contain', voivodeship);

    return MapLocationsSearch;
  }

  static selectNthResult(nth: number) {
    MapLocationsSearch.pageObject.getSearchResults().eq(nth).click();

    return MapLocationsSearch;
  }

  static searchShouldHaveValue(value: string) {
    MapLocationsSearch.pageObject
      .getSearch()
      .should('be.visible')
      .should('have.value', value);

    return MapLocationsSearch;
  }

  static resultsShouldBeClosed() {
    MapLocationsSearch.pageObject.getSearchResultsList().should('not.exist');

    return MapLocationsSearch;
  }

  static clearSearch() {
    MapLocationsSearch.pageObject.getClearSearchBtn().click();

    return MapLocationsSearch;
  }

  static selectActiveResult() {
    MapLocationsSearch.pageObject.getSelectActiveResultBtn().click();

    return MapLocationsSearch;
  }
}
