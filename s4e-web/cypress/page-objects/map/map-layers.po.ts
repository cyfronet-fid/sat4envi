import { Core } from './../core.po';
import { GeneralModal } from '../modal/general-modal.po';

export class Layers extends Core {
  static pageObject = {
    getSidebarManagementBtn: () => cy.get('button[data-e2e="sidebar-management-btn"]'),
    getSidebarLayers: () => cy.get('[data-e2e="layers-list"]').find('[data-e2e="picker-item-label"]'),
    getSelectedLayersIcons: () => cy.get('button[data-e2e="selected-icon"]'),

    getManagementLayers: () => cy.get('tr[data-e2e="management-overlay"]'),
    getRemoveBtns: () => cy.get('button[data-e2e="delete-layer-btn"]'),
    getDisplayInPanelBtns: () => cy.get('[data-e2e="display-in-panel-btn"]'),

    getAddBtn: () => cy.get('[data-e2e="add-layer-btn"]'),
    getLabelInput: () => cy.get('[data-e2e="layer-label-input"]').find('input'),
    getUrlInput: () => cy.get('[data-e2e="layer-url-input"]').find('input'),
    getGroupLabelInput: () => cy.get('[data-e2e="layer-group-label-input"]').find('input'),
    getSubmitFormBtn: () => cy.get('[data-e2e="submit-layer-form-btn"]'),
    getUrlErrors: () => cy.get('[data-e2e="invalid-url-error"]')
  };

  static activeLayersCountShouldBe(count: number) {
    Layers
      .pageObject
      .getSelectedLayersIcons()
      .should('have.length', count);

    return Layers;
  }

  static selectNthSidebarLayer(nth: number) {
    Layers
      .pageObject
      .getSidebarLayers()
      .eq(nth)
      .should('be.visible')
      .click({ force: true });

    return Layers;
  }

  static unselectNthSidebarLayer(nth: number) {
    return Layers.selectNthSidebarLayer(nth);
  }

  static openManagementModal() {
    Layers
      .pageObject
      .getSidebarManagementBtn()
      .should('be.visible')
      .click({ force: true });

    GeneralModal.isVisible();

    return Layers;
  }

  static toggleNthInPanelDisplay(nth: number) {
    Layers
      .pageObject
      .getDisplayInPanelBtns()
      .eq(nth)
      .should('be.visible')
      .click({force: true});

    return Layers;
  }

  static removeNthWithPermission(nth: number) {
    Layers
      .pageObject
      .getRemoveBtns()
      .eq(nth)
      .should('be.visible')
      .click({force: true});

    return Layers;
  }

  static sidebarLayersCountShouldBe(count: number) {
    Layers
      .pageObject
      .getSidebarLayers()
      .should('have.length', count);

    return Layers;
  }

  static managementLayersCountShouldBe(count: number) {
    Layers
      .pageObject
      .getManagementLayers()
      .should('have.length', count);

    return Layers;
  }

  static fillForm(label: string, url: string) {
    Layers
      .pageObject
      .getAddBtn()
      .should('be.visible')
      .click({ force: true });

    Layers
      .pageObject
      .getLabelInput()
      .should('be.visible')
      .type(label);

    Layers
      .pageObject
      .getUrlInput()
      .should('be.visible')
      .type(url);

    Layers
      .pageObject
      .getGroupLabelInput()
      .should('be.visible')
      .type(label);

    return Layers;
  }

  static errorsCountShouldBe(count: number) {
    Layers
      .pageObject
      .getUrlErrors()
      .should('have.length', count);

    return Layers;
  }

  static addNew(label: string, url: string) {
    Layers
      .fillForm(label, url);

    Layers
      .pageObject
      .getSubmitFormBtn()
      .should('be.visible')
      .click({ force: true });

    return Layers;
  }
}
