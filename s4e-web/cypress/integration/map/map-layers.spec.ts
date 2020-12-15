/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { Layers } from '../../page-objects/map/map-layers.po';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';


describe.skip('Map Layers', () => {
  before(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
    cy.fixture('layer-capability.xml').as('geoserverResponse');
  });

  beforeEach(function () {
    cy.visit('/login');
    Login
      .loginAs(this.zkMember);
    cy.get('[data-e2e="layers-list"] [data-e2e="picker-item-label"]').its('length').as('layerCount');
  });

  afterEach(() => {
    Login.forceLogout();
  });

  it('should display selected layer', () => {
    Layers
      .selectNthSidebarLayer(0)
      .activeLayersCountShouldBe(1)
      .unselectNthSidebarLayer(0)
      .activeLayersCountShouldBe(0)
  });

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
    Layers
      .openManagementModal()
      .fillForm(label, geoserverUrl);
    cy.wait('@getCapabilities');

    Layers
      .activeLayersCountShouldBe(28)
      .allUrlLayersCountShouldBe(28);

    GeneralModal
      .closeAndChangeContext(Layers);
  });

  it('should display selected url layers on values in "layers=" query param', function () {
    const label = 'Test';
    const geoserverUrl = '/test-wms';
    const layers = [
      'development:opad_h05_12h',
      'development:rgb24_micro'
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
    Layers
      .openManagementModal()
      .fillForm(label, `${geoserverUrl}?LAYERS=${layers}`);
    cy.wait('@getCapabilities');

    Layers
      .activeLayersCountShouldBe(2)
      .allUrlLayersCountShouldBe(28);

    GeneralModal
      .closeAndChangeContext(Layers);
  });

  it('should add, hide and remove from panel', function () {
    const label = 'Test';
    const url = 'https://eumetview.eumetsat.int/geoserver/wms?LAYERS=eps:metop1_ir108';
    const count = this.layerCount

    Layers
      .openManagementModal()
      .addNew(label, url)
    GeneralModal
      .closeAndChangeContext(Layers)
    Layers
      .sidebarLayersCountShouldBe(count + 1)
      .openManagementModal()
      .toggleNthInPanelDisplay(count)
    GeneralModal
      .closeAndChangeContext(Layers);
    Layers
      .sidebarLayersCountShouldBe(count)
      .openManagementModal()
      .removeNthWithPermission(0)
    ConfirmModal
      .accept();
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

    Layers
      .openManagementModal()
      .addNew(label, url)
      .errorsCountShouldBe(6)
      .changeContextTo(GeneralModal)
      .closeAndChangeContext(Layers);
  });
});
