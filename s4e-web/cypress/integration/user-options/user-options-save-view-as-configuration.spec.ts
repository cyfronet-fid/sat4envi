/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { UserOptionsSaveMapViews } from '../../page-objects/user-options/user-options-save-view-as-configuration.po';
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

  it('should save, display and remove view', () => {MapLayers
    .selectNthSidebarLayer(1)
  UserOptionsSaveMapViews
    .openSaveViewsModal()
    .addView('test-view')
  MapLayers
    .unselectNthSidebarLayer(1)
  UserOptionsSaveMapViews
    .openViewsModal()
    .viewsCountShouldBe(1)
    .selectNth(0)
  MapLayers
    .activeLayersCountShouldBe(1)
  UserOptionsSaveMapViews
    .openViewsModal()
    .deleteNth(0);
  ConfirmModal
    .accept();
  GeneralModal
    .closeModal();
  });
});
