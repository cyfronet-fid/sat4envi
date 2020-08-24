import { Core } from '../core.po';
import promisify from 'cypress-promise';
import 'cypress-file-upload';
import {SideNav} from './settings-side-nav.po';
import {ConfirmModal} from '../modal/confirm-modal.po';
import { InstitutionProfile } from './settings-institution-profile.po';
import { AddInstitutionForm } from './settings-add-institution-form.po';

export class InstitutionList extends Core {
  static pageObject = {
    getNewBtn: () => cy.get('[data-e2e="addInstitution"]'),
    getNthListEntry: (i: number) => cy
      .get(`table[data-e2e="generic-list-data-table"] > tbody > tr[data-e2e="entity-row"]:nth-child(${i + 1})`),
    getAllListEntries: () => cy.get(`table[data-e2e="generic-list-data-table"] > tbody > tr`),
  }

  static goToAddInstitution() {
    InstitutionList.pageObject.getNewBtn().click();
    return AddInstitutionForm;
  }

  static getSideNav() {
    return SideNav;
  }

  static getEntries() {
    return InstitutionList.pageObject.getAllListEntries();
  }

  static async deleteNth(index: number) {
    const length = await promisify(InstitutionList.pageObject.getAllListEntries()
      .should('have.length.gt', 0)
      .its('length'));

    InstitutionList.pageObject.getNthListEntry(index)
      .find('[data-e2e="delete"]')
      .click();

    ConfirmModal.accept();

    InstitutionList.pageObject.getAllListEntries().should('have.length', length - 1);
    return InstitutionList;
  }

  static goToProfile(index: number) {
    InstitutionList.pageObject.getNthListEntry(index).find('a[data-e2e="edit"]').click();
    cy.location('pathname').should('eq', '/settings/institution');
    cy.location('search').should('contain', 'institution=zarzadzenie-kryzysowe-pl');
    return InstitutionProfile;
  }
}