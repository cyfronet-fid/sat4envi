/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapViews } from '../../page-objects/map/map-views.po';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { Layers } from '../../page-objects/map/map-layers.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';

context.skip('Map Views', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(function () {
    Login.loginAs(this.zkMember);
    cy.get('[data-e2e="layers-list"] [data-e2e="picker-item-label"]').should('have.length', 5)
  });

  it('should save view and remove it', () => {
    MapViews
      .openSaveViewsModal()
      .addView('test-view')
      .openViewsModal()
      .viewsCountShouldBe(1)
      .deleteNth(0)
      .changeContextTo(ConfirmModal)
      .acceptAndChangeContextTo(GeneralModal)
      .closeAndChangeContext(MapViews);
  });

  it.only('should display view', () => {
    Layers
      .selectNthSidebarLayer(1)
      .changeContextTo(MapViews)
      .openSaveViewsModal()
      .addView('test-view')
      .changeContextTo(Layers)
      .unselectNthSidebarLayer(1)
      .changeContextTo(MapViews)
      .openViewsModal()
      .viewsCountShouldBe(1)
      .selectNth(0)
      .changeContextTo(Layers)
      .activeLayersCountShouldBe(1)
      .changeContextTo(MapViews)
      .openViewsModal()
      .deleteNth(0)
      .changeContextTo(ConfirmModal)
      .acceptAndChangeContextTo(GeneralModal)
      .closeAndChangeContext(MapViews);
  });

  
});
