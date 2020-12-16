import { Core } from './../core.po';
import { GeneralModal } from '../modal/general-modal.po';

export class MapLayers extends Core {
  static readonly pageObject = {
    getSidebarManagementBtn: () => cy.get('button[data-e2e="sidebar-management-btn"]'),
    getSidebarLayers: () => cy.get('[data-e2e="layers-list"] [data-e2e="picker-item-label"]'), 
    getSelectedLayersIcons: () => cy.get('button[data-e2e="selected-icon"]'),

    getManagementLayers: () => cy.get('tr[data-e2e="management-overlay"]'), 
    getRemoveBtns: () => cy.get('button[data-e2e="delete-layer-btn"]'),
    getDisplayInPanelBtns: () => cy.get('[data-e2e="display-in-panel-btn"]'),

    getAddBtn: () => cy.get('[data-e2e="add-layer-btn"]'),
    getLabelInput: () => cy.get('[data-e2e="layer-label-input"]').find('input'),
    getUrlInput: () => cy.get('[data-e2e="layer-url-input"]'),
    getSubmitFormBtn: () => cy.get('[data-e2e="submit-layer-form-btn"]'),
    getUrlErrors: () => cy.get('[data-e2e="invalid-url-error"]'),
  };


  static activeLayersCountShouldBe(count: number) {

    MapLayers
      .pageObject
      .getSelectedLayersIcons()
      .should('have.length', count);

    return MapLayers;
  }

  static selectNthSidebarLayer(nth: number) {
    MapLayers
      .pageObject
      .getSidebarLayers()
      .eq(nth)
      .click();

      cy.location('href').should('include', 'overlays');

    return MapLayers;
  }

  static unselectNthSidebarLayer(nth: number) {
    MapLayers
    .pageObject
    .getSidebarLayers()
    .eq(nth)
    .click();
    
    return MapLayers;
  }

  static openManagementModal() {
    MapLayers
      .pageObject
      .getSidebarManagementBtn()
      .click();

    GeneralModal.isVisible();

    return MapLayers;
  }

  static toggleNthInPanelDisplay(nth: number) {
    MapLayers
      .pageObject
      .getDisplayInPanelBtns()
      .eq(nth)
      .click();

    return MapLayers;
  }

  static removeNthWithPermission(nth: number) {
    MapLayers
      .pageObject
      .getRemoveBtns()
      .eq(nth)
      .click();

    return MapLayers;
  }

  static sidebarLayersCountShouldBe(count: number) {
    MapLayers
      .pageObject
      .getSidebarLayers()
      .should('have.length', count);

    return MapLayers;
  }

  static managementLayersCountShouldBe(count: number) {
    MapLayers
      .pageObject
      .getManagementLayers()
      .should('have.length', count);

    return MapLayers;
  }

  static fillForm(label: string, url: string) {
    MapLayers
      .pageObject
      .getAddBtn()
      .click();

    MapLayers
      .pageObject
      .getLabelInput()
      .type(label);

    MapLayers
      .pageObject
      .getUrlInput()
      .type(url);

    return MapLayers;
  }

  static errorsCountShouldBe(count: number) {
    MapLayers
      .pageObject
      .getUrlErrors()
      .should('have.length', count);

    return MapLayers;
  }

  static addNew(label: string, url: string) {
    cy.server();
    cy.route('POST', '/api/v1/overlays/personal').as('addedNewLayer');
   
    MapLayers
      .fillForm(label, url);

    MapLayers
      .pageObject
      .getSubmitFormBtn()
      .click();

    return MapLayers;
  };

  static waitForNewAddedLayer(){
    cy.wait('@addedNewLayer');
  }

  static layersConfigurationAreNotVisible(){
    MapLayers
      .pageObject
      .getSidebarManagementBtn()
      .should("not.be.visible");
  }
};
