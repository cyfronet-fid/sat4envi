import { Core } from '../core.po';
import 'cypress-file-upload';
import { InstitutionForm } from '../../../src/app/views/settings/state/institution/institution.model';

export class SettingsInstitutionLayer extends Core {
  static readonly pageObject = {
    getInstitutionLayer: () => cy.get('[data-e2e="layer"]'),
    getAddLayer: () => cy.get('[data-e2e="add-layer-btn"]'),

    getLabelInput: () => cy.get('[data-e2e="layer-label-input"]').find('input'),
    getUrlInput: () => cy.get('[data-e2e="layer-url-input"]'),
    getUrlLayers: () => cy.get('[data-e2e="url-layer"]'),
    getActiveUrlLayers: () => cy.get('[data-e2e="url-layer"]:checked'),
    getSubmitFormBtn: () => cy.get('[data-e2e="submit-layer-form-btn"]'),
    getUrlErrors: () => cy.get('[data-e2e="invalid-url-error"]'),
    getAddedInstitutionLayer: () => cy.get('[data-e2e="management-overlay"]'),
    getDeleteInstitutionLayer: () => cy.get('[data-e2e="delete-layer-btn"]')
  };

  static goToAddInstitutionLayerPage() {
    SettingsInstitutionLayer
      .pageObject
      .getInstitutionLayer()
      .click()

    cy.location('href').should('include', '/settings/institution-wms-overlays');

    return SettingsInstitutionLayer;
  }

  static addLayer() {
    SettingsInstitutionLayer
      .pageObject
      .getAddLayer()
      .click()

    return SettingsInstitutionLayer
  }

  static fillForm(label: string, url: string, waitForResponse = true) {
    SettingsInstitutionLayer
      .pageObject
      .getUrlInput()
      .type(url)

    if (waitForResponse) { cy.wait('@getCapabilities') };

    SettingsInstitutionLayer
      .pageObject
      .getLabelInput()
      .type(label);

    return SettingsInstitutionLayer;
  }

  static addNew() {
    cy.route('POST', '/api/v1/institutions/*/overlays').as('addedInstitutionLayer');

    SettingsInstitutionLayer
      .pageObject
      .getSubmitFormBtn()
      .click();

    cy.wait('@addedInstitutionLayer', { timeout: 20000 });

    return SettingsInstitutionLayer;
  }

  static institutionLayersCountShouldBe(count: number) {
    SettingsInstitutionLayer
      .pageObject
      .getAddedInstitutionLayer()
      .should('have.length', count);

    return SettingsInstitutionLayer;
  }

  static deleteNthInstitutionLayer(nth: number) {
    SettingsInstitutionLayer
      .pageObject
      .getDeleteInstitutionLayer()
      .eq(nth)
      .click()

    return SettingsInstitutionLayer;
  }
};
