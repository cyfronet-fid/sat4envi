import { Core } from '../core.po';
import 'cypress-file-upload';
import { InstitutionForm } from '../../../src/app/views/settings/state/institution/institution.model';
import { slugify } from '../../utils/slugify.util'

export class SettingsInstitutions extends Core {
  static readonly pageObject = {
    getInstitutionsList: () => cy.get('[data-e2e="institution-name"]'),
    getInstitutionsCount: () => cy.get('[data-e2e="institution-name"]').its('length').as('institutionsCount'),
    getInstitutionChildrenList: () => cy.get('[data-e2e="childrenInstitution"]'),
    getAddChildBtn: () => cy.get('[data-e2e="add-child-btn"]'),
    getEditInstitutionBtn: () => cy.get('[data-e2e="edit-institution"]'),
    getAddNewInstitutionBtn: () => cy.get('[data-e2e="addInstitution"]'),
    getRemoveInstitutionClass: '[data-e2e="delete"]',
    getSelectedInstitution: () => cy.get('[data-e2e="selectedInstitution"]'),
    getInstitutionDetails: () => cy.get('[data-e2e="institution-details"]'),
    getErrors: () => cy.get('.error'),
    getLogoImage: () => cy.get('[data-e2e="emblem-image"]'),
    getParentNameInput: () => cy.get('[data-e2e="parentName"]').find('input'),
    getParentModalContainer: () => cy.get('[data-e2e="modal-container"]'),
    getAllParentInstitutionName: () => cy.get('[data-e2e="selectParentInstitution"]'),
    getAssignToParentInstitutionBtn: () => cy.get('[data-e2e="btn-submit"]'),
    getNameInput: () => cy.get('[data-e2e="name"]').find('input'),
    getStreetInput: () => cy.get('[data-e2e="address"]').find('input'),
    getZipCodeInput: () => cy.get('[data-e2e="postalCode"]').find('input'),
    getCityInput: () => cy.get('[data-e2e="city"]').find('input'),
    getPhoneInput: () => cy.get('[data-e2e="phone"]').find('input'),
    getSecondaryPhoneInput: () => cy.get('[data-e2e="secondaryPhone"]').find('input'),
    getLogoInput: () => cy.get('[data-e2e="emblem-input"]'),
    getEmailInput: () => cy.get('[data-e2e="emails"]'),
    getSubmitBtn: () => cy.get('[data-e2e="submit-btn"]'),
    getCancelBtn: () => cy.get('[data-e2e="cancel-btn"]'),
  };

  static selectNthInstitution(nth: number) {
    SettingsInstitutions
      .pageObject
      .getInstitutionsList()
      .eq(nth)
      .click()

    cy.location('href').should('include', '/settings/institution?institution');

    return SettingsInstitutions;
  }

  static selectInstitutionByName(name: string) {
    SettingsInstitutions
      .pageObject
      .getInstitutionsList()
      .contains(name)
      .click()

    cy.location('href').should('include', '/settings/institution?institution');

    return SettingsInstitutions;
  }

  static goToAddChildInstitutionPage() {
    SettingsInstitutions
      .pageObject
      .getAddChildBtn()
      .click()

    cy.location('href').should('include', '/settings/add-institution');

    return SettingsInstitutions;
  }

  static goToAddNewInstitutionPage() {
    SettingsInstitutions
      .pageObject
      .getAddNewInstitutionBtn()
      .click();

    return SettingsInstitutions;
  }

  static goToEditInstitutionPage() {
    SettingsInstitutions
      .pageObject
      .getEditInstitutionBtn()
      .click();

    cy.location('href').should('include', 'settings/edit-institution');

    return SettingsInstitutions;
  }

  static editForm(name: string, institution: InstitutionForm) {
    SettingsInstitutions
      .pageObject
      .getCityInput()
      .clear()
      .type(name);

    SettingsInstitutions
      .pageObject
      .getEmailInput()
      .type(institution.adminsEmails)

    return SettingsInstitutions;
  }

  static removeInstitution(name: string) {
    SettingsInstitutions
      .pageObject
      .getInstitutionsList()
      .contains(name)
      .parent()
      .parent()
      .find(SettingsInstitutions.pageObject.getRemoveInstitutionClass)
      .click();

    return SettingsInstitutions;
  }

  static fillForm(institution: InstitutionForm, parentInstution = true) {

    if (parentInstution) {
      SettingsInstitutions
        .pageObject
        .getParentNameInput()
        .click()
      SettingsInstitutions
        .pageObject
        .getParentModalContainer()
        .should("be.visible")
      SettingsInstitutions
        .pageObject
        .getParentModalContainer()
        .should("be.visible")
      SettingsInstitutions
        .pageObject
        .getAllParentInstitutionName()
        .first()
        .click({ force: true })
      SettingsInstitutions
        .pageObject
        .getAssignToParentInstitutionBtn()
        .click()
    }

    SettingsInstitutions
      .pageObject
      .getNameInput()
      .type(institution.name);
    SettingsInstitutions
      .pageObject
      .getStreetInput()
      .type(institution.address);
    SettingsInstitutions
      .pageObject
      .getZipCodeInput()
      .type(institution.postalCode);
    SettingsInstitutions
      .pageObject
      .getCityInput()
      .type(institution.city);
    SettingsInstitutions
      .pageObject
      .getPhoneInput()
      .type(institution.phone);
    SettingsInstitutions
      .pageObject
      .getSecondaryPhoneInput()
      .type(institution.secondaryPhone);
    SettingsInstitutions
      .pageObject
      .getLogoInput()
      .attachFile('emblem.png');
    SettingsInstitutions
      .pageObject
      .getLogoImage()
      .should('be.visible');

    return SettingsInstitutions;
  }

  static submit() {
    SettingsInstitutions
      .pageObject
      .getSubmitBtn()
      .click();

    return SettingsInstitutions;
  }

  static shouldDisplayInstitutionProfile(name: string) {

    const institutionName = slugify(name)
    cy.location('href').should('include', `/settings/institution?institution=${institutionName}`);

    SettingsInstitutions
      .pageObject
      .getInstitutionDetails()
      .should("contain.text", name)

    return SettingsInstitutions;
  }

  static shouldBeOnInstitutionList(name: string) {
    SettingsInstitutions
      .pageObject
      .getInstitutionsList()
      .should("contain.text", name)

    return SettingsInstitutions;
  }

  static shouldBeOnInstitutionChildrenList(name: string) {
    SettingsInstitutions
      .pageObject
      .getInstitutionChildrenList()
      .should("contain.text", name)

    return SettingsInstitutions;
  }

  static selectInstitutionChildByName(name: string) {
    SettingsInstitutions
      .pageObject
      .getInstitutionChildrenList()
      .contains(name)
      .click()

    cy.location('href').should('include', '/settings/institution?institution');

    return SettingsInstitutions;
  }

  static editedInstitutionShouldContain(cityName: string) {
    SettingsInstitutions
      .pageObject
      .getInstitutionDetails()
      .should("contain.text", cityName)

    return SettingsInstitutions;
  }

  static selectedInstitutionShouldBe(institutionName: string) {
    SettingsInstitutions
      .pageObject
      .getSelectedInstitution()
      .should("contain.text", institutionName)

    return SettingsInstitutions;
  }

  static errorsCountShouldBe(count: number) {
    SettingsInstitutions
      .pageObject
      .getErrors()
      .should('have.length', count);

    return SettingsInstitutions;
  }

  static allInstititutionCountShouldBe(count: number) {
    SettingsInstitutions
      .pageObject
      .getInstitutionsList()
      .should('have.length', count);

    return SettingsInstitutions;
  }

  static addChildAndEditInstitutionShould(value: string) {
    SettingsInstitutions
      .pageObject
      .getAddChildBtn()
      .should(value)

    SettingsInstitutions
      .pageObject
      .getEditInstitutionBtn()
      .should(value)

    return SettingsInstitutions;
  }
}
