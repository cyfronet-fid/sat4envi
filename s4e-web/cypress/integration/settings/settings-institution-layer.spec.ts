/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { UserOptionsGoToSettings } from "../../page-objects/user-options/user-options-go-to-settings-profile.po";
import { SettingsInstitutionLayer } from "../../page-objects/settings/settings-institution-layer.po";
import { SettingsInstitutions } from "../../page-objects/settings/settings-institution-profile.po";
import { MapLayers } from "../../page-objects/map/map-layers.po";
import { ConfirmModal } from "../../page-objects/modal/confirm-modal.po";
import { SettingsNav } from "../../page-objects/settings/settings-navigation.po"

before(() => {
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('layer-capability.xml').as('geoserverResponse');
  cy.fixture('scene.png').as('scene');
});

describe('Institution Layer', () => {

  beforeEach(function () {
    cy.visit('/login')
    Login
      .loginAs(this.zkAdmin)
    MapLayers.pageObject.getLayerCount();
  });

  it('should add layer to institution', function () {
    const label = 'Test';
    const geoserverUrl = '/wms';
    const layer = [
      'main:opad_h05_12h']

    cy.server();
    cy.route({
      method: 'GET',
      url: `${geoserverUrl}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`,
      headers: {
        'Content-type': 'text/xml'
      },
      status: 200,
      response: this.geoserverResponse
    })
      .as('getCapabilities');

    cy.route({
      method: 'GET',
      url: `https://localhost:4200/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=true&LAYERS=${layer}&*`,
      headers: {
        'Content-type': 'image/png'
      },
      status: 200,
      response: this.scene
    })

    UserOptionsGoToSettings
      .gotoUserProfile();
    SettingsInstitutions
      .selectNthInstitution(0);
    SettingsInstitutionLayer
      .goToAddInstitutionLayerPage()
      .institutionLayersCountShouldBe(0)
      .addLayer()
      .fillForm(label, `${geoserverUrl}?LAYERS=${layer}`)
      .addNew()
      .institutionLayersCountShouldBe(1);
    SettingsNav
      .returnToMap();
    MapLayers
      .sidebarLayersCountShouldBe(this.layerCount + 1);
    UserOptionsGoToSettings
      .gotoUserProfile();
    SettingsInstitutions
      .selectNthInstitution(0);
    SettingsInstitutionLayer
      .goToAddInstitutionLayerPage()
      .institutionLayersCountShouldBe(1)
      .deleteNthInstitutionLayer(0);
    ConfirmModal
      .accept();
    SettingsInstitutionLayer
      .institutionLayersCountShouldBe(0);
    SettingsNav
      .returnToMap();
    MapLayers
      .sidebarLayersCountShouldBe(this.layerCount);
  });
});
