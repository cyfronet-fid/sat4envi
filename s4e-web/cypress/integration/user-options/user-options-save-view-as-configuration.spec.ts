/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {UserOptionsSaveMapViews} from '../../page-objects/user-options/user-options-save-view-as-configuration.po';
import {GeneralModal} from '../../page-objects/modal/general-modal.po';
import {MapLayers} from '../../page-objects/map/map-layers.po';
import {ConfirmModal} from '../../page-objects/modal/confirm-modal.po';
import {MapDateSelect} from '../../page-objects/map/map-date-select.po';
import {MapProducts} from '../../page-objects/map/map-products.po';
import {UserOptionsSendView} from '../../page-objects/user-options/user-options-send-view-to-mail.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('products.json').as('products');
});

describe('Map Views', () => {
  beforeEach(function () {
    cy.visit('/login');
    Login.loginAs(this.zkMember);
  });

  it('should save, display and remove configuration for the recent scene', function () {
    const year = 2020;
    const month = 2;
    const day = 6;

    MapProducts.selectProductByName(this.products[0].name);
    MapDateSelect.openDateChange().selectDate(year, month, day);

    MapLayers.selectNthSidebarLayer(1);
    UserOptionsSaveMapViews.openSaveViewsModal().saveViewForRecentScene('test-view');
    MapLayers.unselectNthSidebarLayer(1);
    UserOptionsSaveMapViews.openViewsModal()
      .viewsCountShouldBe(1)
      .loadNthViewWithRecentScene(0);
    MapLayers.activeLayersCountShouldBe(1);
    UserOptionsSaveMapViews.openViewsModal().deleteNth(0);
    ConfirmModal.accept();
    GeneralModal.closeModal();
  });

  it('should save, display and remove configuration for the current scene', function () {
    cy.deleteAllMails();

    const year = 2020;
    const month = 2;
    const day = 6;

    MapProducts.selectProductByName(this.products[0].name);
    MapDateSelect.openDateChange().selectDate(year, month, day);

    MapLayers.selectNthSidebarLayer(1);
    UserOptionsSaveMapViews.openSaveViewsModal().saveViewForCurrentScene(
      'test-view'
    );
    MapLayers.unselectNthSidebarLayer(1);
    UserOptionsSaveMapViews.openViewsModal()
      .viewsCountShouldBe(1)
      .loadNthViewWithCurrentScene(0, year, month, day);
    MapLayers.activeLayersCountShouldBe(1);
    UserOptionsSaveMapViews.openViewsModal().sendNthSavedView(0);
    UserOptionsSendView.fillFields(
      'test@mail.pl',
      'caption-test',
      'description-test'
    ).sendView();
    UserOptionsSaveMapViews.openViewsModal().deleteNth(0);
    ConfirmModal.accept();
    GeneralModal.closeModal();

    UserOptionsSendView.clickShareView();
  });
});
