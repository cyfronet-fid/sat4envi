import {InstitutionFactory} from '../../../src/app/views/settings/state/institution/institution.factory.spec';
import {Login} from '../../page-objects/login/login.po';
import {InstitutionList} from '../../page-objects/settings/institution-list.po';
/// <reference types="Cypress" />

context('Institutions', () => {
  beforeEach(() => {
    cy.fixture('users/zkAdmin.json').as('zkAdmin');
  });

  beforeEach(function () {
    Login.loginAs(this.zkAdmin)
      .goToSettingsAs(this.zkAdmin)
      .goToInstitutionList();
  });

  context('Add institution form', () => {
    it('Should add new institution', () => {
      InstitutionList
        .goToAddInstitution()
        .openParentInstitutionModal()
        .selectFirstParentInstitution()
        .submitAndClose()
        .fillFormWith(InstitutionFactory.build())
        .submit()
        .getSideNav()
        .logout();
    });
  });

  context('Institution list', () => {
    it('Should show institutions', () => {
      InstitutionList.getEntries().should('have.length', 2);
    });

    it('should delete institution', () => {
      InstitutionList.deleteEntry(1);
    });

    it('should go to Profile', () => {
      InstitutionList.goToProfile(0);
    });
  });
});
