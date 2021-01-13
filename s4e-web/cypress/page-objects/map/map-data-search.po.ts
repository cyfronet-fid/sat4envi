import { Core } from '../core.po';

export class MapDataSearch extends Core {

  static readonly pageObject = {
    getGoToSearchDataBtn: () => cy.get('[data-e2e="data-sentinel-search"]'),
    getSentinelsBtn: () => cy.get('[data-e2e="sentinel-checkbox"]'),
    getSearchBtn: () => cy.get('[data-e2e="btn-search"]'),
    getResultList: () => cy.get('[data-e2e="sentinel-search-result"]'),
    getAllResultsSeparately: () => cy.get('[data-e2e="search-result-entry"]'),
    getAllResultsDownloadBtn: () => cy.get('[data-e2e="download-link"]'),
    getAllResultsDetailsBtn: () => cy.get('[data-e2e="show-details-button"]'),
    getDetailsModal: () => cy.get('[data-e2e="modal-body"]'),
    getResultDownloadAllArtifactsBtn: () => cy.get('[data-e2e="btn--download-all"]')
  }

  static goToSearchData() {
    cy.server()
    cy.route('GET', "/api/v1/config/search").as("search")

    MapDataSearch
      .pageObject
      .getGoToSearchDataBtn()
      .click();

    cy.wait('@search')

    return MapDataSearch
  }

  static selectNthProduct(number: number) {

    cy.location('href').should('include', '/map/sentinel-search');

    MapDataSearch
      .pageObject
      .getSentinelsBtn()
      .eq(number)
      .should("be.visible")
      .then((element) => {
        element.trigger('click') //for avoid detached from DOM
      })

    return MapDataSearch
  }

  static search() {
    cy.server()
    cy.route('GET', "/api/v1/search/*").as("loadedData")

    cy.location('href').should('include', 'selected');

    MapDataSearch
      .pageObject
      .getSearchBtn()
      .should("be.visible")
      .then((element)=>{
        element.trigger('click') //for avoid detached from DOM
      })

    MapDataSearch
      .pageObject
      .getResultList()
      .should("be.visible");

    cy.wait('@loadedData')

    return MapDataSearch
  }

  static downloadNthData(number: number) {

    MapDataSearch
      .pageObject
      .getAllResultsDownloadBtn()
      .eq(number)
      .invoke('removeAttr', 'target')
      .click()

    return MapDataSearch
  }

  static selectNthDataDetails(number: number) {
    MapDataSearch
      .pageObject
      .getAllResultsDetailsBtn()
      .eq(number)
      .click()

    MapDataSearch
      .pageObject
      .getDetailsModal()
      .should('be.visible')

    return MapDataSearch
  }

  static downloadArtifacts() {
    MapDataSearch
      .pageObject
      .getResultDownloadAllArtifactsBtn()
      .invoke('removeAttr', 'target')
      .click()

    return MapDataSearch
  }

  static shouldReturnToLoginPage() {
    cy.location('pathname').should('eq', '/login');
  }
};
