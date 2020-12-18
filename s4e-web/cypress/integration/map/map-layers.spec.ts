/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapLayers } from '../../page-objects/map/map-layers.po';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Map Layers', () => {

  context("Configuration layers for not logged in user", () => {

    beforeEach(function () {
      cy.visit('/login');
    });

    it('configuration layers shouldn\'t be visible without authentication', () => {
      Login.
        goToMapWithoutLogin();
      MapLayers
        .layersConfigurationAreNotVisible();
    });
  });

  context("Configuration layers for logged in user", () => {

    beforeEach(function () {
      cy.visit('/login');
      Login.loginAs(this.zkMember);
      MapLayers.pageObject.getLayerCount();
    });

    it('should display selected layer', () => {
      MapLayers
        .selectNthSidebarLayer(0)
        .activeLayersCountShouldBe(1)
        .unselectNthSidebarLayer(0)
        .activeLayersCountShouldBe(0);
    });

    it('should add, hide and remove layer from panel', function () {
      const label = 'Test';
      const url = 'https://eumetview.eumetsat.int/geoserver/wms?LAYERS=eps:metop1_ir108';

      MapLayers
        .openManagementModal()
        .addNew(label, url)
        .waitForNewAddedLayer();
      GeneralModal
        .closeModal();
      MapLayers
        .sidebarLayersCountShouldBe(this.layerCount + 1)
        .openManagementModal()
        .toggleNthInPanelDisplay(this.layerCount);
      GeneralModal
        .closeModal();
      MapLayers
        .sidebarLayersCountShouldBe(this.layerCount)
        .openManagementModal()
        .removeNthWithPermission(0);
      ConfirmModal
        .accept();
      GeneralModal
        .closeModal();
      MapLayers
        .sidebarLayersCountShouldBe(this.layerCount);
      
    });

    it('should display url errors', function () {
      const label = 'Test';
      const url = [
        'https://test-page.pl/?SERVICE=unknown',
        'REQUEST=unknown',
        'LAYERS=unkn?own',
        'STYLES=unkn?own',
        'FORMAT=image/unknown',
        'TRANSPARENT=none',
        'VERSION=x.y.z',
        'HEIGHT=none',
        'WIDTH=none',
        'CRS=unknown',
        'BBOX=unknown,unknown,unknown,unknown'
      ].join('&');

      MapLayers
        .openManagementModal()
        .addNew(label, url)
        .errorsCountShouldBe(6);
      GeneralModal
        .closeModal();
    });
  });
});