/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/login.po';
import { Layers } from '../../page-objects/map/map-layers.po';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';


describe('Map Layers', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login.loginAs(this.zkMember);
    cy.get('[data-e2e="layers-list"] [data-e2e="picker-item-label"]').its('length').as('layerCount');
  });


  it('should display selected layer', () => {
    Layers
      .selectNthSidebarLayer(0)
      .activeLayersCountShouldBe(1)
      .unselectNthSidebarLayer(0)
      .activeLayersCountShouldBe(0)
  });


  it.only('should add, hide and remove from panel', function () {
    const label = 'Test';
    const url = 'https://eumetview.eumetsat.int/geoserver/wms?LAYERS=eps:metop1_ir108';
    const count = this.layerCount

    Layers
      .openManagementModal()
      .addNew(label, url)
      .changeContextTo(GeneralModal)
      .closeAndChangeContext(Layers)

    Layers
      .sidebarLayersCountShouldBe(count + 1)
      .openManagementModal()
      .toggleNthInPanelDisplay(count)
      .changeContextTo(GeneralModal)
      .closeAndChangeContext(Layers)
      .sidebarLayersCountShouldBe(count)
      .openManagementModal()
      .removeNthWithPermission(0)
      .changeContextTo(ConfirmModal)
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
