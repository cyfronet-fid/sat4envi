/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/login.po';
import { Layers } from '../../page-objects/map/map-layers.po';
import promisify from 'cypress-promise';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';
import { Map } from '../../page-objects/map/map.po';

context('Map Layers', () => {
  before(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
    cy.fixture('emblem.png', 'binary').as('imageWms');
  });

  before(function () {
    Login.loginAs(this.zkMember);
  });

  context('Image WMS URL validation', () => {
    before(function () {
      cy.server({
        method: 'GET',
        delay: 500,
        status: 200,
        response: this.imageWms
      });
    });

    it('should display selected layer', () => {
      Layers
        .selectNthSidebarLayer(0)
        .activeLayersCountShouldBe(1)
        .selectNthSidebarLayer(0)
        .activeLayersCountShouldBe(0);
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
        .errorsCountShouldBe(6);
    });
    it.only('should add and remove from panel', async () => {
      const label = 'Test';
      const url = 'http://localhost:5000/wms';
      cy.route(url + '**');

      const initialSize = (await promisify(Layers.pageObject.getSidebarLayers())).length;
      Layers
        .openManagementModal()
        .addNew(label, url)
        .changeContextTo(GeneralModal)
        .closeAndChangeContext(Layers)
        .sidebarLayersCountShouldBe(initialSize + 1)

        .openManagementModal()
        .toggleNthInPanelDisplay(initialSize)
        .changeContextTo(GeneralModal)
        .closeAndChangeContext(Layers)
        .sidebarLayersCountShouldBe(initialSize)

        .openManagementModal()
        .removeNthWithPermission(0)
        .changeContextTo(ConfirmModal)
        .accept();
    });
    it('should add new layer and remove it', async () => {
      const label = 'Test';
      const url = '/test**';
      cy.route(url + '**');

      const initialSize = (await promisify(
        Layers
          .openManagementModal()
          .pageObject
          .getManagementLayers()
      )).length;
      Layers
        .addNew(label, url)
        .managementLayersCountShouldBe(initialSize + 1)
        .removeNthWithPermission(0)
        .changeContextTo(ConfirmModal)
        .acceptAndChangeContextTo(Layers)
        .managementLayersCountShouldBe(initialSize)
        .changeContextTo(GeneralModal)
        .closeAndChangeContext(Layers);
    });
  });
});
