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
      .should('be.visible')
      .type(institution.name);
    AddInstitutionForm
      .pageObject
      .getStreetInput()
      .should('be.visible')
      .type(institution.address);
    AddInstitutionForm
      .pageObject
      .getZipCodeInput()
      .should('be.visible')
      .type(institution.postalCode);
    AddInstitutionForm
      .pageObject
      .getCityInput()
      .should('be.visible')
      .type(institution.city);
    AddInstitutionForm
      .pageObject
      .getPhoneInput()
      .should('be.visible')
      .type(institution.phone);
    AddInstitutionForm
      .pageObject
      .getLogoInput()
      .attachFile('emblem.png');
    AddInstitutionForm
      .pageObject
      .getLogoImage()
      .should('be.visible');
    AddInstitutionForm
      .pageObject
      .getEmailInput()
      .type(institution.institutionAdminEmail);

    return AddInstitutionForm;
  }

  static submit() {
    AddInstitutionForm
      .pageObject
      .getSubmitBtn()
      .should('be.visible')
      .click();

    return AddInstitutionForm;
  }
}
