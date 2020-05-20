import { InstitutionFactory } from '../../../src/app/views/settings/state/institution/institution.factory.spec';
/// <reference types="Cypress" />

import promisify from 'cypress-promise';
import { Login } from '../../page-objects/login/login.po';

context('Add institution form', () => {
  let spy: any;
  Cypress.on('window:before:load', (win) => {
    spy = cy.spy(win.console, 'error');
  });

  it('Should add new institution', async () => {
    const user = await promisify(cy.fixture('users/zkAdmin.json'));
    Login
      .loginAs(user)
      .goToSettingsAs(user)
      .goToAddInstitution()
      .openParentInstitutionModal()
      .selectFirstParentInstitution()
      .submitAndClose()
      .fillFormWith(InstitutionFactory.build())
      .submit()
      .getSideNav()
      .logout();

    // TODO: IMPORTANT!!! Uncomment this after institution backend add child will appear
    // await promisify(cy.wait(100));
    // expect(spy).not.to.be.called;
  });

});
