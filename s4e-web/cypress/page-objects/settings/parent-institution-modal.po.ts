import { AddInstitutionForm } from './add-institution-form.po';

export namespace ParentInstitutionModal {
  export class PageObject {
    // TODO: update elements with data-e2e attributes
    static getModal = () => cy.get('s4e-generic-modal');
    static getSearchInput = () => cy.get('s4e-generic-modal .search__input');
    static getInstitutionsRadios = () => cy.get('s4e-generic-modal label[for="institution"]');
    static getSubmitBtn = () => cy.get('s4e-generic-modal .button').first();
  }

  export function selectFirstParentInstitution(searchValue: string = ' ') {
    AddInstitutionForm.PageObject
      .getParentNameInput()
      .should('be.visible')
      .click();
    PageObject
      .getModal()
      .should('be.visible');
    PageObject
      .getSearchInput()
      .should('be.visible')
      .type(searchValue);
    PageObject
      .getInstitutionsRadios()
      .should('be.visible')
      .first()
      .click();

    return ParentInstitutionModal;
  }

  export function submitAndClose() {
    PageObject
      .getSubmitBtn()
      .should('be.visible')
      .click();
    PageObject
      .getModal()
      .should('not.be.visible');

    return AddInstitutionForm;
  }
}

