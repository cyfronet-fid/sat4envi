import 'cypress-file-upload';
import {SideNav} from './side-nav.po';
import {AddInstitutionForm} from './add-institution-form.po';
import {ConfirmModal} from '../modal/confirm.po';
import {InstitutionProfile} from './institution-profile.po';

export namespace InstitutionList {
  export class PageObject {
    static getNewBtn = () => cy.get('[data-e2e="addInstitution"]');
    static getNthListEntry = (i: number) => cy
      .get(`table[data-e2e="generic-list-data-table"] > tbody > tr[data-e2e="entity-row"]:nth-child(${i + 1})`);
    static getAllListEntries = () => cy.get(`table[data-e2e="generic-list-data-table"] > tbody > tr`);
  }

  export function goToAddInstitution() {
    PageObject.getNewBtn().click();
    return AddInstitutionForm;
  }

  export function getSideNav() {
    return SideNav;
  }

  export function getEntries() {
    return PageObject.getAllListEntries();
  }

  export function deleteEntry(index: number) {
    PageObject.getAllListEntries().should('have.length', 2);
    PageObject.getNthListEntry(index).find('[data-e2e="delete"]').click();
    ConfirmModal.accept();
    PageObject.getAllListEntries().should('have.length', 1);
    return InstitutionList;
  }

  export function goToProfile(index: number) {
    PageObject.getNthListEntry(index).find('a[data-e2e="edit"]').click();
    cy.location('pathname').should('eq', '/settings/institution');
    cy.location('search').should('contain', 'institution=zarzadzenie-kryzysowe-pl');
    return InstitutionProfile;
  }
}
