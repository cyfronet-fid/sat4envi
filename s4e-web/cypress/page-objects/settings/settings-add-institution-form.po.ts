import { Core } from './../core.po';
import 'cypress-file-upload';
import { ParentInstitutionModal } from './settings-parent-institution-modal.po';
import { SideNav } from './settings-side-nav.po';
import { Institution } from '../../../src/app/views/settings/state/institution/institution.model';

export class AddInstitutionForm extends Core {
  static pageObject = {
    getAlertMessage:  () => cy.get('[data-e2e="alert-message"]'),
    getAlertMessageDetails:  () => cy.get('[data-e2e="alert-message-details"]'),

    getLogoImage:  () => cy.get('[data-e2e="emblem-image"]'),

    getParentNameInput:  () => cy.get('#parentName'),
    getNameInput:  () => cy.get('#name'),
    getStreetInput:  () => cy.get('#address'),
    getZipCodeInput:  () => cy.get('#postalCode'),
    getCityInput:  () => cy.get('#city'),
    getPhoneInput:  () => cy.get('#phone'),
    getLogoInput:  () => cy.get('[data-e2e="emblem-input"]'),
    getEmailInput:  () => cy.get('#institutionAdminEmail'),
    getSubmitBtn:  () => cy.get('[data-e2e="submit-btn"]'),
  };

  static openParentInstitutionModal() {
    return ParentInstitutionModal;
  }

  static fillFormWith(institution: Institution) {
    AddInstitutionForm
      .pageObject
      .getNameInput()
      .type(institution.name);
    AddInstitutionForm
      .pageObject
      .getStreetInput()
      .type(institution.address);
    AddInstitutionForm
      .pageObject
      .getZipCodeInput()
      .type(institution.postalCode);
    AddInstitutionForm
      .pageObject
      .getCityInput()
      .type(institution.city);
    AddInstitutionForm
      .pageObject
      .getPhoneInput()
      .type(institution.phone);
    AddInstitutionForm
      .pageObject
      .getLogoInput()
      .attachFile('emblem.png');
    AddInstitutionForm
      .pageObject
      .getLogoImage()
      .should('be.visible');

    return AddInstitutionForm;
  }

  static submit() {
    AddInstitutionForm
      .pageObject
      .getSubmitBtn()
      .click();

    return AddInstitutionForm;
  }
}
