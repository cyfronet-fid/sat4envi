/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { SaveMapViews } from '../../page-objects/user-options/user-options-save-view-as-configuration.po';
import { GeneralModal } from '../../page-objects/modal/general-modal.po';
import { MapLayers } from '../../page-objects/map/map-layers.po';
import { ConfirmModal } from '../../page-objects/modal/confirm-modal.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Map Views', () => {
  beforeEach(function () {
    cy.visit("/login")
    Login.loginAs(this.zkMember);

  });

  it('should save view and remove it', () => {
    SaveMapViews
      .openSaveViewsModal()
      .addView('test-view')
      .openViewsModal()
      .viewsCountShouldBe(1)
      .deleteNth(0)
    ConfirmModal
      .accept()
    SaveMapViews
      .viewsCountShouldBe(0)
    GeneralModal
      .closeModal();
  });

  it('should display view', () => {
    MapLayers
      .selectNthSidebarLayer(1)
    SaveMapViews
      .openSaveViewsModal()
      .addView('test-view')
    MapLayers
      .unselectNthSidebarLayer(1)
    SaveMapViews
      .openViewsModal()
      .viewsCountShouldBe(1)
      .selectNth(0)
    MapLayers
      .activeLayersCountShouldBe(1)
    SaveMapViews
      .openViewsModal()
      .deleteNth(0);
    ConfirmModal
      .accept();
    GeneralModal
      .closeModal();
  });
});
