/// <reference types="Cypress" />

import { Login } from '../../page-objects/login/login.po';
import { Layers } from '../../page-objects/map/map-layers.po';
import promisify from 'cypress-promise';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';

context('Map Layers', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login.loginAs(this.zkMember);
  });

  it('should display selected layer', () => {
    Layers
      .selectNthSidebarLayer(0)
      .activeLayersCountShouldBe(1)
      .selectNthSidebarLayer(0)
      .activeLayersCountShouldBe(0);
  });
  it('should add and remove from panel', async () => {
    const label = 'Test';
    const url = 'http://test.pl:5000';
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
    const url = 'http://test.pl:5000';
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
