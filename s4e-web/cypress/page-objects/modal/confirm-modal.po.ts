import { Core } from '../core.po';

export class ConfirmModal extends Core {
  static pageObject = {
    getAcceptBtn: () => cy.get('s4e-confirm-modal #accept_btn'),
    getCancelBtn: () => cy.get('s4e-confirm-modal #cancel_btn'),
  }

  static accept() {
    ConfirmModal
      .pageObject
      .getAcceptBtn()
      .click();
  }

  static acceptAndChangeContextTo<T>(context: T) {
    ConfirmModal.accept();

    return context;
  }

  static cancelAndChangeContextTo<T>(context: T) {
    ConfirmModal
      .pageObject
      .getCancelBtn()
      .click();

    return context;
  }
}
