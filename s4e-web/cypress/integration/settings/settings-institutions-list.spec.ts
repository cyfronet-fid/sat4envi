/// <reference types="Cypress" />

import {InstitutionFactory} from '../../../src/app/views/settings/state/institution/institution.factory.spec';
import {Login} from '../../page-objects/auth/login.po';
import {InstitutionList} from '../../page-objects/settings/settings-institution-list.po';
import promisify from 'cypress-promise';
import { InstitutionSearch } from './../../page-objects/settings/settings-institution-search.po';

context('Settings institutions list', () => {
  beforeEach(() => {
    cy.fixture('users/zkAdmin.json').as('zkAdmin');
  });

  beforeEach(function () {
    Login
      .loginAs(this.zkAdmin)
      .goToSettingsAs(this.zkAdmin)
      .goToInstitutionList();
  });

  // TODO: Repair errors and refactor
  // clicking on institution input provide to start page, it should stay at the same
  // institution is returned with 200, but never added
  // it('Should add new institution', async () => {
  //   InstitutionSearch.openResults();
  //   const initialSize = (await promisify(InstitutionSearch.pageObject.getSearchResults())).length;
  //   InstitutionList
  //     .goToAddInstitution()
  //     .openParentInstitutionModal()
  //     .selectFirstParentInstitution()
  //     .submitAndClose()
  //     .fillFormWith(InstitutionFactory.build())
  //     .submit()
  //     .changeContextTo(InstitutionSearch)
  //     .openResults()
  //       .pageObject
  //       .getSearchResults()
  //       .should('have.length', initialSize + 1);
  // });
  // it('should delete institution', async () => {
  //   await InstitutionList.deleteNth(1);
  // });

  it('Should show institutions', () => {
    InstitutionList.getEntries().should('have.length', 5);
  });

  it('should go to Profile', async () => {
    const institutionName = await InstitutionList.getNthInstitutionName(0);
    InstitutionList
      .goToInstitutionProfile(0)
      .changeContextTo(InstitutionSearch)
      .shouldHaveValue(institutionName);
  });
});


