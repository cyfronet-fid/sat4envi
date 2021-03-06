import {Core} from './../core.po';

export class GeneralModal extends Core {
  static readonly pageObject = {
    getCloseBtn: () => cy.get('[data-e2e="close-btn"]'),
    getCancelBtn: () => cy.get('[data-e2e="btn-cancel"]'),
    getModalContainer: () => cy.get('[data-e2e="modal-container"]')
  };

  static isClosed() {
    GeneralModal.pageObject.getModalContainer().should('not.exist');

    return GeneralModal;
  }

  static isVisible() {
    GeneralModal.pageObject.getModalContainer().should('be.visible');

    return GeneralModal;
  }

  static closeModal() {
    GeneralModal.pageObject.getCloseBtn().click();
    GeneralModal.pageObject.getModalContainer().should('not.exist');

    return GeneralModal;
  }

  static cancelModal() {
    GeneralModal.pageObject.getCancelBtn().click();
    GeneralModal.pageObject.getModalContainer().should('not.exist');

    return GeneralModal;
  }
}
