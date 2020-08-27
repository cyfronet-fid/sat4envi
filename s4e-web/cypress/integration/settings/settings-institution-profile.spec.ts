/// <reference types="Cypress" />

import { Login } from '../../page-objects/login/login.po';
import { InstitutionSearch } from '../../page-objects/settings/settings-institution-search.po';
import { SideNav } from '../../page-objects/settings/settings-side-nav.po';
import { InstitutionProfile } from '../../page-objects/settings/settings-institution-profile.po';
import promisify from 'cypress-promise';
import { InstitutionFactory } from '../../../src/app/views/settings/state/institution/institution.factory.spec';

context('Settings institution profile', () => {
  beforeEach(() => {
    cy.fixture('users/zkAdmin.json').as('zkAdmin');
  });

  beforeEach(function () {
    Login
      .loginAs(this.zkAdmin)
      .goToSettingsAs(this.zkAdmin)
      .changeContextTo(SideNav)
      .goToNthInstitutionProfile(0);
  });

  it('should display institution description', async () => {
    const institutionLabel = await promisify(
      InstitutionSearch
          .pageObject
          .getSearch()
          .invoke('val')
    );

    cy.wait(500);

    InstitutionProfile
      .pageObject
      .getInstitutionDetails()
      .should('contain', institutionLabel);

    InstitutionProfile
      .pageObject
      .getEmblem()
      .should('be.visible');

    InstitutionProfile
      .pageObject
      .getPostalCodeWithCity()
      .should('be.visible');
  });

  // TODO: Uncomment and refactor after repairing addition of children
  // it('should add institution child, display and remove it', async () => {
  //   const parentInstitutionLabel = await promisify(
  //     InstitutionSearch
  //         .pageObject
  //         .getSearch()
  //         .invoke('val')
  //   );

  //   // add child
  //   const institution = InstitutionFactory.build();
  //   InstitutionProfile
  //     .childrenCountShouldBe(0)
  //     .goToAddChildForm()
  //     .openParentInstitutionModal()
  //     .selectFirstParentInstitution()
  //     .submitAndClose()
  //     .fillFormWith(institution)
  //     .submit();

  //   InstitutionProfile
  //     .pageObject
  //     .getInstitutionDetails()
  //     .should('have.text', institution.name);

  //   InstitutionProfile
  //     .pageObject
  //     .getEmblem()
  //     .should('exist');

  //   InstitutionProfile
  //     .pageObject
  //     .getPostalCodeWithCity()
  //     .should('have.text', institution.city);

  //   // remove child
  //   InstitutionSearch
  //     .selectNthInstitutionResultByLabel(1, parentInstitutionLabel)
  //     .changeContextTo(InstitutionProfile)
  //     .childrenCountShouldBe(1)
  //     .removeNthChild(1)
  //     .childrenCountShouldBe(0);
  // });
});
