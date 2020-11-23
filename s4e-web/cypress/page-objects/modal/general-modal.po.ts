import { Core } from './../core.po';

export class GeneralModal extends Core {
  static pageObject = {
    getCloseBtnClass: '[data-e2e="close-btn"]',
    getCloseBtn: () => cy.get(GeneralModal.pageObject.getCloseBtnClass),
    getCancelBtn: () => cy.get('button.button-cancel'),
    getModalContainer: () => cy.get('[data-e2e="modal-container"]')
  };

  static isClosed() {
    GeneralModal
      .pageObject
      .getModalContainer()
      .should('not.be.visible');

    return GeneralModal;
  }

  static isVisible() {
    GeneralModal
      .pageObject
      .getModalContainer() 
      .should('be.visible');
  }

  static closeAndChangeContext<T>(context: T) {
    GeneralModal
      .pageObject
      .getCloseBtn()
      .click({ force: true });
    GeneralModal
      .pageObject
      .getModalContainer()
      .should('not.exist');

    return context;
  }

  static cancelAndChangeContext<T>(context: T) {
    GeneralModal
      .pageObject
      .getCancelBtn()
      .click({ force: true });
    GeneralModal
      .pageObject
      .getModalContainer()
      .should('not.exist');

    return context;
  }
}
