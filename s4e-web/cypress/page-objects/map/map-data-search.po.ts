import { Core } from '../core.po';

export class MapDataSearch extends Core {

  static readonly pageObject = {
    getGoToSearchDataBtn: () => cy.get('[data-e2e="data-sentinel-search"]'),
    getSentinelsBtn: () => cy.get('[data-e2e="sentinel-section-header"]'),
    getSearchBtn: () => cy.get('[data-e2e="btn-search"]'),
    getResultList: () => cy.get('[data-e2e="sentinel-search-result"]'),
    getAllResultsSeparately: () => cy.get('[data-e2e="search-result-entry"]'),
    getAllResultsDownloadBtn:() => cy.get('[data-e2e="download-link"]'),
    getAllResultsDetailsBtn: ()=> cy.get('[data-e2e="show-details-button"]'),
    getDetailsModal: () => cy.get('[data-e2e="modal-body"]'),
    getResultDownloadAllArtifactsBtn:() => cy.get('[data-e2e="btn--download"]')
  }

  static goToSearchData() {
    MapDataSearch
      .pageObject
      .getGoToSearchDataBtn()
      .click();

    return MapDataSearch
  }

  static selectNthProductToSearch(number: number) {
    MapDataSearch
      .pageObject
      .getSentinelsBtn()
      .eq(number)
      .click();

    return MapDataSearch
  }

  static searchData() {
    cy.server()
    cy.route('GET', "/api/v1/search/*").as("loadedData")
    
    MapDataSearch
      .pageObject
      .getSearchBtn()
      .click();

    MapDataSearch
      .pageObject
      .getResultList()
      .should("be.visible");

    cy.wait('@loadedData')

    return MapDataSearch
  }

  static selectNthDataToDownload(number:number){
    MapDataSearch
      .pageObject
      .getAllResultsDownloadBtn()
      .eq(number)
      .invoke('removeAttr', 'target')
      .click()

    return MapDataSearch
  }

  static selectNthDataDetails(number:number){
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

  static downloadArtifacts(){
    MapDataSearch
     .pageObject
     .getResultDownloadAllArtifactsBtn()
     .invoke('removeAttr', 'target')
     .click()

     return MapDataSearch
  }

  static shouldReturnToLoginPage(){
    cy.location('pathname').should('eq', '/login');
  }
};