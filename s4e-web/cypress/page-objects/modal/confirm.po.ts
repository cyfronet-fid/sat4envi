export namespace ConfirmModal {
  export class PageObject {
    static accept = () => cy.get('s4e-confirm-modal #accept_btn');
    static cancel = () => cy.get('s4e-confirm-modal #cancel_btn');
  }

  export function accept() {
    PageObject.accept().click();
  }

  export function cancel() {
    PageObject.cancel().click();
  }
}
