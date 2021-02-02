/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapLayers } from '../../page-objects/map/map-layers.po';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('layer-capability.xml').as('geoserverResponse');
  cy.fixture('image.png').as('image');
});

describe('Map layers', () => {

  context("Configuration layers for not logged in user", () => {
    beforeEach(function () {
      cy.visit('/login');
    });

    it('configuration layers shouldn\'t be visible without authentication', () => {
      Login.
        goToMap();
      MapLayers
        .layersConfigurationAreNotVisible();
    });
  });

  context("Configuration layers for logged in user", () => {

    beforeEach(function () {
      cy.visit('/login')
      Login.loginAs(this.zkMember);
      MapLayers.pageObject.getLayerCount();
    })

    it('should display all url layers', function () {
      const label = 'Test';
      const geoserverUrl = '/test-wms';

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


      MapLayers
        .openManagementModal()
        .fillForm(label, geoserverUrl);
      MapLayers
        .selectedUrlLayersCountShouldBe(29)
        .allUrlLayersCountShouldBe(29);
      GeneralModal
        .closeModal();
    });

    it('should display selected url layers on values in "layers=" query param', function () {
      const label = 'Test';
      const geoserverUrl = '/test-wms';
      const layers = [
        'main:opad_h05_12h',
        'main:rgb24_micro'
      ].join(',');

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


      MapLayers
        .openManagementModal()
        .fillForm(label, `${geoserverUrl}?LAYERS=${layers}`)
        .selectedUrlLayersCountShouldBe(2)
        .allUrlLayersCountShouldBe(29);
      GeneralModal
        .closeModal();
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
      const geoserverUrl = '/wms';
      const layers = [
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
        url: `https://localhost:4200/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=true&LAYERS=${layers}&*`,
        headers: {
            'Content-type': 'image/png'
        },
        status: 200,
        response: this.image
      });

      MapLayers
        .openManagementModal()
        .fillForm(label, `${geoserverUrl}?LAYERS=${layers}`)
        .addNew()
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
  });
    context("Error handling", () => {

      beforeEach(function () {
        cy.visit('/login')
        Login.loginAs(this.zkMember);
      })

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
          .fillForm(label, url, false)
          .errorsCountShouldBe(6);
        GeneralModal
          .closeModal();
      });

      it('handle 400 error', function () {
        const label = 'Test';
        const geoserverUrl = '/test-wms';

        cy.server();
        cy.route({
          method: 'GET',
          url: `${geoserverUrl}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`,
          headers: {
            'Content-type': 'text/xml'
          },
          status: 400,
          response: { errors: 'CannotGET /...' }
        })
          .as('getCapabilities');
        MapLayers
          .openManagementModal()
          .fillForm(label, geoserverUrl, false)
          .errorShouldBeDispalyed();
        GeneralModal
          .closeModal();
      });

      it('handle 404 error', function () {
        const label = 'Test';
        const geoserverUrl = '/test-wms';

        cy.server();
        cy.route({
          method: 'GET',
          url: `${geoserverUrl}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`,
          headers: {
            'Content-type': 'text/xml'
          },
          status: 404,
          response: { errors: 'CannotGET /...' }
        })
          .as('getCapabilities');

        MapLayers
          .openManagementModal()
          .fillForm(label, geoserverUrl, false)
          .errorShouldBeDispalyed();
        GeneralModal
          .closeModal();
      });

      it('handle 500 error', function () {
        const label = 'Test';
        const geoserverUrl = '/test-wms';

        cy.server();
        cy.route({
          method: 'GET',
          url: `${geoserverUrl}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`,
          headers: {
            'Content-type': 'text/xml'
          },
          status: 500,
          response: { errors: 'CannotGET /...' }
        })
          .as('getCapabilities');
        MapLayers
          .openManagementModal()
          .fillForm(label, geoserverUrl, false)
          .errorShouldBeDispalyed();
        GeneralModal
          .closeModal();
      });

      it('handle 503 error', function () {
        const label = 'Test';
        const geoserverUrl = '/test-wms';

        cy.server();
        cy.route({
          method: 'GET',
          url: `${geoserverUrl}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`,
          headers: {
            'Content-type': 'text/xml'
          },
          status: 503,
          response: { errors: 'CannotGET /...' }
        })
          .as('getCapabilities');

        MapLayers
          .openManagementModal()
          .fillForm(label, geoserverUrl, false)
          .errorShouldBeDispalyed();
        GeneralModal
          .closeModal();
      });

      it('handle response does not have the correct formats ', function () {
        const label = 'Test';
        const geoserverUrl = '/test-wms';
        const incorrectResponse = "Response does not have the correct formats"

        cy.server();
        cy.route({
          method: 'GET',
          url: `${geoserverUrl}?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities`,
          headers: {
            'Content-type': 'text/xml'
          },
          status: 200,
          response: incorrectResponse
        })
          .as('getCapabilities');

        MapLayers
          .openManagementModal()
          .fillForm(label, geoserverUrl)
          .errorShouldBeDispalyed();
        GeneralModal
          .closeModal();
      });
    });
});
