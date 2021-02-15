import {Core} from './../core.po';

export class UserOptionsSaveMapViews extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getOpenSaveViewsBtn: () => cy.get('[data-e2e="open-save-view-modal-btn"]'),
    getOpenViewsBtn: () => cy.get('[data-e2e="open-views-modal-btn"]'),
    getViews: () => cy.get('[data-e2e="view"]'),
    getViewsDeleteBtns: () => cy.get('[data-e2e="view-delete-btn"]'),
    getViewLabelInput: () => cy.get('[data-e2e="view-label"]').find('input'),
    getAddBtn: () => cy.get('[data-e2e="add-view-btn"]'),
    getViewLoadBtns: () => cy.get('[data-e2e="view-load-btn"]'),
    getCurrentSceneCheckBox: () => cy.get('[data-e2e="include-scene-control"]')
  };

  static openSaveViewsModal() {
    cy.location('href').should('include', '/map/products?');

    UserOptionsSaveMapViews.pageObject.getOptionsBtn().click();

    UserOptionsSaveMapViews.pageObject.getOpenSaveViewsBtn().click();

    return UserOptionsSaveMapViews;
  }

  static openViewsModal() {
    cy.location('href').should('include', '/map/products?');

    UserOptionsSaveMapViews.pageObject.getOptionsBtn().click();

    UserOptionsSaveMapViews.pageObject.getOpenViewsBtn().click();

    return UserOptionsSaveMapViews;
  }

  static saveViewForCurrentScene(label: string) {
    UserOptionsSaveMapViews.pageObject
      .getViewLabelInput()
      .should('be.visible')
      .clear()
      .type(label);

    UserOptionsSaveMapViews.pageObject.getAddBtn().click();

    return UserOptionsSaveMapViews;
  }

  static saveViewForRecentScene(label: string) {
    UserOptionsSaveMapViews.pageObject.getCurrentSceneCheckBox().click();

    UserOptionsSaveMapViews.pageObject
      .getViewLabelInput()
      .should('be.visible')
      .clear()
      .type(label);

    UserOptionsSaveMapViews.pageObject.getAddBtn().click();

    return UserOptionsSaveMapViews;
  }

  static viewsCountShouldBe(count: number) {
    UserOptionsSaveMapViews.pageObject.getViews().should('have.length', count);

    return UserOptionsSaveMapViews;
  }

  static deleteNth(nth: number) {
    UserOptionsSaveMapViews.pageObject.getViewsDeleteBtns().eq(nth).click();

    return UserOptionsSaveMapViews;
  }

  static loadViewWithRecentScene() {
    cy.route('GET', '/api/v1/products/*/scenes/most-recent?{*,*/*}').as(
      'loadRecentScene'
    );
    cy.route('/api/v1/products/*/scenes/available?{*,*/*}').as('availableScene');

    UserOptionsSaveMapViews.pageObject.getViewLoadBtns().eq(0).click();

    cy.wait('@loadRecentScene');
    cy.wait('@availableScene');

    return UserOptionsSaveMapViews;
  }

  static loadViewWithCurrentScene(year: number, month: number, day: number) {
    cy.route(
      'GET',
      `/api/v1/products/*/scenes?date=${year}-${month < 10 ? '0' + month : month}-${
        day < 10 ? '0' + day : day
      }{*,*/*}`
    ).as('loadCurrentScene');
    cy.route('/api/v1/products/*/scenes/available?{*,*/*}').as('availableScene');

    UserOptionsSaveMapViews.pageObject.getViewLoadBtns().eq(0).click();

    cy.wait('@loadCurrentScene');
    cy.wait('@availableScene');

    return UserOptionsSaveMapViews;
  }
}
