import 'cypress-file-upload';
import { ParentInstitutionModal } from './parent-institution-modal.po';
import { SideNav } from './side-nav.po';
import { Institution } from '../../../src/app/views/settings/state/institution/institution.model';

export namespace AddInstitutionForm {
  export class PageObject {
    static getAlertMessage = () => cy.get('[data-e2e="alert-message"]');
    static getAlertMessageDetails = () => cy.get('[data-e2e="alert-message-details"]');

    static getLogoImage = () => cy.get('[data-e2e="emblem-image"]');

    static getParentNameInput = () => cy.get('#parentName');
    static getNameInput = () => cy.get('#name');
    static getStreetInput = () => cy.get('#address');
    static getZipCodeInput = () => cy.get('#postalCode');
    static getCityInput = () => cy.get('#city');
    static getPhoneInput = () => cy.get('#phone');
    static getLogoInput = () => cy.get('[data-e2e="emblem-input"]');
    static getEmailInput = () => cy.get('#institutionAdminEmail');
    static getSubmitBtn = () => cy.get('[data-e2e="submit-btn"]');
  }

  export function openParentInstitutionModal() {
    return ParentInstitutionModal;
  }

  export function fillFormWith(institution: Institution) {
    PageObject
      .getNameInput()
      .should('be.visible')
      .type(institution.name);
    PageObject
      .getStreetInput()
      .should('be.visible')
      .type(institution.address);
    PageObject
      .getZipCodeInput()
      .should('be.visible')
      .type(institution.postalCode);
    PageObject
      .getCityInput()
      .should('be.visible')
      .type(institution.city);
    PageObject
      .getPhoneInput()
      .should('be.visible')
      .type(institution.phone);
    PageObject
      .getLogoInput()
      .attachFile('emblem.png');
    PageObject
      .getLogoImage()
      .should('be.visible');
    PageObject
      .getEmailInput()
      .type(institution.institutionAdminEmail);

    return AddInstitutionForm;
  }

  export function submit() {
    PageObject
      .getSubmitBtn()
      .should('be.visible')
      .click();

    return AddInstitutionForm;
  }

  export function getSideNav() {
    return SideNav;
  }
}
