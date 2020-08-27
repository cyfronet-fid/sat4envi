import { Core } from './../core.po';
import { AddInstitutionForm } from './settings-add-institution-form.po';
export class InstitutionProfile extends Core {
  static pageObject = {
    getInstitutionDetails: () => cy.get('[data-e2e="institution-details"]'),
    getEmblem: () => cy.get('[data-e2e="institution-emblem"]'),
    getPostalCodeWithCity: () => cy.get('[data-e2e="postal-code-with-city"]'),
    getAddChildBtn: () => cy.get('[data-e2e="add-child-btn"]'),
    getRemoveChildBtns: () => cy.get('[data-e2e="delete"]'),
    getChildren: () => cy.get('[data-e2e="entity-row"]')
  };

  static removeNthChild(nth: number) {
    InstitutionProfile
      .pageObject
      .getRemoveChildBtns()
      .eq(nth)
      .should('be.visible')
      .click({ force: true });

    return InstitutionProfile;
  }

  static goToAddChildForm() {
    InstitutionProfile
      .pageObject
      .getAddChildBtn()
      .should('be.visible')
      .click();

    return AddInstitutionForm;
  }

  static childrenCountShouldBe(count: number) {
    InstitutionProfile
      .pageObject
      .getChildren()
      .should('have.length', count);

    return InstitutionProfile;
  }
}
