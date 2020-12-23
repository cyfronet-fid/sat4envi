import { Core } from './../core.po';

export class SaveMapViews extends Core {
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
    SaveMapViews
      .pageObject
      .getOptionsBtn()
      .click();

    SaveMapViews
      .pageObject
      .getOpenSaveViewsBtn()
      .click();

    return SaveMapViews;
  }

  static openViewsModal() {
    SaveMapViews
      .pageObject
      .getOptionsBtn()
      .click();

    SaveMapViews
      .pageObject
      .getOpenViewsBtn()
      .click();

    return SaveMapViews;
  }

  static addView(label: string) {
    SaveMapViews
      .pageObject
      .getViewLabelInput()
      .should("be.visible")
      .clear()
      .type(label);

    SaveMapViews
      .pageObject
      .getAddBtn()
      .click();

    return SaveMapViews;
  }

  static viewsCountShouldBe(count: number) {
    SaveMapViews
      .pageObject
      .getViews()
      .should('have.length', count);

    return SaveMapViews;
  }

  static deleteNth(nth: number) {
    SaveMapViews
      .pageObject
      .getViewsDeleteBtns()
      .eq(nth)
      .click();

    return SaveMapViews;
  }

  static selectNth(nth: number) {
    SaveMapViews
      .pageObject
      .getViewLoadBtns()
      .eq(nth)
      .click();

    return SaveMapViews;
  }
}