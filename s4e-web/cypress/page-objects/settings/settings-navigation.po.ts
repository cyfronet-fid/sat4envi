import { Core } from '../core.po';

export class SettingsNav extends Core {
  static readonly pageObject = {
    getInstitutionsListBtn: () => cy.get('[data-e2e="institutionsList"]'),
    getUserProfileBtn: () => cy.get('data-e2e="userProfile"'),
    getReturnToMapBtn: () => cy.get('[data-e2e="returnToMap"]'),
    getLogOutBtn: () => cy.get('[data-e2e="logOut"]'),
    getChangeInstitutionBtn: () => cy.get('[data-e2e="changeInstitution"]'),
    getManagePrivilegeBtn: () => cy.get('[data-e2e="managePrivilege"]')
  };

  static logOut() {
    SettingsNav
      .pageObject
      .getLogOutBtn()
      .click();

    cy.location('pathname').should('eq', '/login');

    return SettingsNav;
  }

  static changeInstitution() {
    SettingsNav
      .pageObject
      .getChangeInstitutionBtn()
      .click();

    cy.location('pathname').should('eq', '/settings/institutions');

    return SettingsNav;
  }

  static returnToMap() {
    SettingsNav
      .pageObject
      .getReturnToMapBtn()
      .click();

    cy.location('href').should('include', '/map/products');

    return SettingsNav;
  }

  static goToUserProfile() {
    SettingsNav
      .pageObject
      .getUserProfileBtn()
      .click();

    cy.location('pathname').should('eq', '/settings/profile');

    return SettingsNav;
  }

  static goToInstitutionsList() {
    SettingsNav
      .pageObject
      .getInstitutionsListBtn()
      .click();

    cy.location('pathname').should('eq', '/settings/institutions');

    return SettingsNav;
  }

  static goToManagePrivilege() {
    SettingsNav
      .pageObject
      .getManagePrivilegeBtn()
      .click();

    cy.location('pathname').should('eq', '/settings/manage-authorities');

    return SettingsNav;
  }

  static institutionNavigationShouldHaveValue(value: string) {
    SettingsNav
      .pageObject
      .getChangeInstitutionBtn()
      .should(value)

    SettingsNav
      .pageObject
      .getInstitutionsListBtn()
      .should(value)

    return SettingsNav;
  }
}