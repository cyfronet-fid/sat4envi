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
    getViewLoadBtns: () => cy.get('[data-e2e="view-load-btn"]')
  };

  static openSaveViewsModal() {
    UserOptionsSaveMapViews.pageObject.getOptionsBtn().click();

    UserOptionsSaveMapViews.pageObject.getOpenSaveViewsBtn().click();

    return UserOptionsSaveMapViews;
  }

  static openViewsModal() {
    UserOptionsSaveMapViews.pageObject.getOptionsBtn().click();

    UserOptionsSaveMapViews.pageObject.getOpenViewsBtn().click();

    return UserOptionsSaveMapViews;
  }

  static addView(label: string) {
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

  static selectNth(nth: number) {
    UserOptionsSaveMapViews.pageObject.getViewLoadBtns().eq(nth).click();

    return UserOptionsSaveMapViews;
  }
}
