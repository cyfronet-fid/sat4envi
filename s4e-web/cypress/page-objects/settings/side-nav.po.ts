import { Login } from '../login/login.po';
import { AddInstitutionForm } from './add-institution-form.po';
import { Profile } from './profile.po';

export namespace SideNav {
  export class PageObject {
    // TODO: update elements with data-e2e attributes
    static getAddInstitutionBtn = () => cy.get('li[data-e2e="addInstitution"] a');
    static getProfileBtn = () => cy.get('li[data-e2e="profile"] a');
    static getLogoutBtn = () => cy.get('.login a');
  }

  export function goToAddInstitution() {
    PageObject
      .getAddInstitutionBtn()
      .should('be.visible')
      .click();

    cy.location('pathname').should('eq', '/settings/add-institution');

    return AddInstitutionForm;
  }

  export function goToProfile() {
    PageObject
      .getProfileBtn()
      .should('be.visible')
      .click();

    cy.location('pathname').should('eq', '/settings/profile');

    return Profile;
  }

  export function logout() {
    PageObject
      .getLogoutBtn()
      .should('be.visible')
      .click();
    cy.location('pathname').should('eq', '/login');

    return Login;
  }

}
