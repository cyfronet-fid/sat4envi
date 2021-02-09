import {Core} from './../core.po';
import {GeneralModal} from '../modal/general-modal.po';

export class MapLayers extends Core {
  static readonly pageObject = {
    getSidebarManagementBtn: () =>
      cy.get('button[data-e2e="sidebar-management-btn"]'),
    getSidebarLayers: () =>
      cy.get('[data-e2e="layers-list"] [data-e2e="picker-item-label"]'),
    getSelectedLayersIcons: () => cy.get('button[data-e2e="selected-icon"]'),

    getManagementLayers: () => cy.get('tr[data-e2e="management-overlay"]'),
    getRemoveBtns: () => cy.get('button[data-e2e="delete-layer-btn"]'),
    getDisplayInPanelBtns: () => cy.get('[data-e2e="display-in-panel-btn"]'),

    getAddBtn: () => cy.get('[data-e2e="add-layer-btn"]'),
    getLabelInput: () => cy.get('[data-e2e="layer-label-input"]').find('input'),
    getUrlInput: () => cy.get('[data-e2e="layer-url-input"]'),
    getUrlLayers: () => cy.get('[data-e2e="url-layer"]'),
    getActiveUrlLayers: () => cy.get('[data-e2e="url-layer"]:checked'),
    getSubmitFormBtn: () => cy.get('[data-e2e="submit-layer-form-btn"]'),
    getUrlErrors: () => cy.get('[data-e2e="invalid-url-error"]'),
    getLayerCount: () =>
      cy
        .get('[data-e2e="layers-list"] [data-e2e="picker-item-label"]')
        .its('length')
        .as('layerCount'),
    getErrorMessage: () => cy.get('.message')
  };

  static activeLayersCountShouldBe(count: number) {
    MapLayers.pageObject.getSelectedLayersIcons().should('have.length', count);

    return MapLayers;
  }

  static selectNthSidebarLayer(nth: number) {
    MapLayers.pageObject.getSidebarLayers().eq(nth).click();

    cy.location('href').should('include', 'overlays');

    return MapLayers;
  }

  static unselectNthSidebarLayer(nth: number) {
    MapLayers.pageObject.getSidebarLayers().eq(nth).click();

    return MapLayers;
  }

  static openManagementModal() {
    MapLayers.pageObject.getSidebarManagementBtn().click();

    GeneralModal.isVisible();

    return MapLayers;
  }

  static toggleNthInPanelDisplay(nth: number) {
    MapLayers.pageObject.getDisplayInPanelBtns().eq(nth).click();

    return MapLayers;
  }

  static removeNthWithPermission(nth: number) {
    MapLayers.pageObject.getRemoveBtns().eq(nth).click();

    return MapLayers;
  }

  static sidebarLayersCountShouldBe(count: number) {
    MapLayers.pageObject.getSidebarLayers().should('have.length', count);

    return MapLayers;
  }

  static managementLayersCountShouldBe(count: number) {
    MapLayers.pageObject.getManagementLayers().should('have.length', count);

    return MapLayers;
  }

  static fillForm(label: string, url: string, waitForResponse = true) {
    MapLayers.pageObject.getAddBtn().click();

    MapLayers.pageObject.getUrlInput().type(url);

    if (waitForResponse) {
      cy.wait('@getCapabilities');
    }

    MapLayers.pageObject.getLabelInput().type(label);

    return MapLayers;
  }

  static allUrlLayersCountShouldBe(count: number) {
    MapLayers.pageObject.getUrlLayers().should('have.length', count);

    return MapLayers;
  }

  static selectedUrlLayersCountShouldBe(count: number) {
    MapLayers.pageObject.getActiveUrlLayers().should('have.length', count);

    return MapLayers;
  }

  static errorsCountShouldBe(count: number) {
    MapLayers.pageObject.getUrlErrors().should('have.length', count);

    return MapLayers;
  }

  static addNew() {
    cy.server();
    cy.route('POST', '/api/v1/overlays/personal').as('addedNewLayer');

    MapLayers.pageObject.getSubmitFormBtn().click();

    cy.wait('@getMap', {timeout: 10000});
    cy.wait('@addedNewLayer', {timeout: 10000});

    return MapLayers;
  }

  static layersConfigurationAreNotVisible() {
    MapLayers.pageObject.getSidebarManagementBtn().should('not.exist');

    return MapLayers;
  }

  static errorShouldBeDispalyed() {
    MapLayers.pageObject.getErrorMessage().should('be.visible');

    return MapLayers;
  }
}
