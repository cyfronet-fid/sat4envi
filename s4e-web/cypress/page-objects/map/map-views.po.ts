import { Core } from './../core.po';

export class MapViews extends Core {
  static pageObject = {
    getZkOptionsBtn: () => cy.get('[data-e2e="zk-options-btn"]'), // button zkOptions
    getOpenSaveViewsBtn: () => cy.get('[data-e2e="open-save-view-modal-btn"]'), // button "zapisz konfiguracje widoku lista"
    getOpenViewsBtn: () => cy.get('[data-e2e="open-views-modal-btn"]'), // button "wczytaj zapisana konfegeracje lista"
    getViews: () => cy.get('[data-e2e="view"]'), // "wczytaj konfiguracje" window
    getViewsDeleteBtns: () => cy.get('[data-e2e="view-delete-btn"]'), // delete configuration
    getViewLabelInput: () => cy.get('[data-e2e="view-label"]').find('input'), // label of name configuration
    getAddBtn: () => cy.get('[data-e2e="add-view-btn"]'), // save configuration
    getViewLoadBtns: () => cy.get('[data-e2e="view-load-btn"]')//wczytaj
  };

  static openSaveViewsModal() {
    MapViews
      .pageObject
      .getZkOptionsBtn()
      .click();

    MapViews
      .pageObject
      .getOpenSaveViewsBtn()
      .click({ force: true });

    return MapViews;
  }

  static openViewsModal() {
    MapViews
      .pageObject
      .getZkOptionsBtn()
      .click();

    MapViews
      .pageObject
      .getOpenViewsBtn()
      .click({ force: true });

    return MapViews;
  }

  static addView(label: string) {
    MapViews
      .pageObject
      .getViewLabelInput()
      .should("be.visible")
      .clear({ force: true })
      .type(label, { force: true });

    MapViews
      .pageObject
      .getAddBtn()
      .click({ force: true });

    return MapViews;
  }

  static viewsCountShouldBe(count: number) {
    MapViews
      .pageObject
      .getViews()
      .should('have.length', count);

    return MapViews;
  }

  static deleteNth(nth: number) {
    MapViews
      .pageObject
      .getViewsDeleteBtns()
      .eq(nth)
      .click({ force: true });

    return MapViews;
  }

  static selectNth(nth: number) {
    MapViews
      .pageObject
      .getViewLoadBtns()
      .eq(nth)
      .click({ force: true });

    return MapViews;
  }
}
