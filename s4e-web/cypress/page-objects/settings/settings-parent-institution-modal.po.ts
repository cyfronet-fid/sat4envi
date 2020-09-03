import { Core } from '../core.po';
import { AddInstitutionForm } from './settings-add-institution-form.po';

export class ParentInstitutionModal extends Core {
  static pageObject = {
    // TODO: update elements with data-e2e attributes
    getModal: () => cy.get('s4e-generic-modal'),
    getSearchInput: () => cy.get('s4e-generic-modal .search__input'),
    getInstitutionsRadios: () => cy.get('s4e-generic-modal label[data-e2e="institution-radio"]'),
    getSubmitBtn: () => cy.get('s4e-generic-modal .button').first(),
  };

  static selectFirstParentInstitution(searchValue: string = ' ') {
    AddInstitutionForm
      .pageObject
      .getParentNameInput()
      .should('be.visible')
      .click();
    ParentInstitutionModal
      .pageObject
      .getModal()
      .should('be.visible');
    ParentInstitutionModal
      .pageObject
      .getSearchInput()
      .should('be.visible')
      .type(searchValue);
    ParentInstitutionModal
      .pageObject
      .getInstitutionsRadios()
      .should('be.visible')
      .first()
      .click();

    return ParentInstitutionModal;
  }

  static submitAndClose() {
    ParentInstitutionModal
      .pageObject
      .getSubmitBtn()
      .should('be.visible')
      .click();
    ParentInstitutionModal
      .pageObject
      .getModal()
      .should('not.be.visible');

    return AddInstitutionForm;
  }
}

