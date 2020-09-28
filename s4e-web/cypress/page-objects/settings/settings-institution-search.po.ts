import { Core } from '../core.po';

export class InstitutionSearch extends Core {
  static pageObject = {
    getSearch: () => cy.get('[data-e2e="search-input"]'),
    getSearchResults: () => cy.get('[data-e2e="search-result"]'),
    getSearchResultsList: () => cy.get('[data-e2e="search-results-list"]'),
    getClearSearchBtn: () => cy.get('[data-e2e="clear-search-btn"]'),
    getSelectActiveResultBtn: () => cy.get('[data-e2e="select-active-result-btn"]')
  };

  static openResults() {
    InstitutionSearch
      .pageObject
      .getSearch()
      .click();

    return InstitutionSearch;
  }

  static shouldHaveValue(value: string) {
    InstitutionSearch
      .pageObject
      .getSearch()
      .invoke('val')
      .should('be.a', value);

    return InstitutionSearch;
  }

  static searchFor(label: string) {
    InstitutionSearch
      .pageObject
      .getSearch()
      .type(label);

    return InstitutionSearch;
  }

  static selectNthInstitutionResultByLabel(nth: number, label: any) {
    InstitutionSearch.searchFor(label);

    InstitutionSearch
      .pageObject
      .getSearchResults()
      .eq(nth)
      .click({ force: true });

    return InstitutionSearch;
  }

  static selectNthInstitutionResult(nth: number) {
    InstitutionSearch.openResults();

    InstitutionSearch
      .pageObject
      .getSearchResults()
      .eq(nth)
      .click({ force: true });

    return InstitutionSearch;
  }
}
