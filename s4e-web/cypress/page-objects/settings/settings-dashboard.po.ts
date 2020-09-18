import { UserProfile } from './settings-user-profile.po';
import { InstitutionProfile } from './settings-institution-profile.po';
import { InstitutionPeople } from './settings-institution-people.po';
import { Core } from '../core.po';
import promisify from 'cypress-promise';

export class AdminDashboard extends Core {
  static pageObject = {
    getTiles: () => cy.get('s4e-tile'),

    getGoToPeopleListTile: () => cy
      .get('[data-e2e="go-to-people-list-tile"]'),
    getGoToPeopleListBtn: () => AdminDashboard
      .pageObject
      .getGoToPeopleListTile()
      .find('button'),

    getGoToInstitutionProfileTile: () => cy
      .get('[data-e2e="go-to-institution-profile-tile"]'),
    getGoToInstitutionProfileBtn: () => AdminDashboard
      .pageObject
      .getGoToPeopleListTile()
      .find('button'),
  };

  static async tilesShouldContainHeadersDescriptionsAndImages() {
    const tilesNumber = (await promisify(
      AdminDashboard
        .pageObject
        .getTiles()
    )).length;

    AdminDashboard
      .pageObject
      .getTiles()
      .should('be.visible')
      .find('header')
      .should('be.visible')
      .should('have.length', tilesNumber);

    AdminDashboard
      .pageObject
      .getTiles()
      .should('be.visible')
      .find('p')
      .should('be.visible')
      .should('have.length', tilesNumber);

    AdminDashboard
      .pageObject
      .getTiles()
      .should('be.visible')
      .find('img')
      .should('be.visible')
      .should('have.length', tilesNumber);
  }

  static goToPeopleList() {
    return InstitutionPeople.goTo(
      AdminDashboard.pageObject.getGoToPeopleListBtn(),
      '/settings/profile',
      UserProfile
    );
  }

  static goToInstitutionProfile() {
    return InstitutionPeople.goTo(
      AdminDashboard.pageObject.getGoToInstitutionProfileBtn(),
      '/settings/institution',
      InstitutionProfile
    );
  }
}

export class SuperAdminDashboard extends Core {
  static pageObject = {
    getInstitutionSearchTile: () => cy.get('[data-e2e="institution-search-tile"]'),
    getGoToInstitutionsTile: () => cy.get('[data-e2e="go-to-institutions-list-tile"]'),
    getGoToInstitutionsListBtn: () => SuperAdminDashboard
      .pageObject
      .getGoToInstitutionsTile()
      .find('buttton')
  };

  static goToInstitutionsList() {
    return InstitutionPeople.goTo(
      AdminDashboard.pageObject.getGoToPeopleListBtn(),
      '/settings/institutions',
      InstitutionProfile
    );
  }

  static async tilesShouldContainHeadersDescriptionsAndImages() {
    const tilesNumber = (await promisify(
      AdminDashboard
        .pageObject
        .getTiles()
    )).length;

    AdminDashboard
      .pageObject
      .getTiles()
      .should('be.visible')
      .find('header')
      .should('be.visible')
      .should('have.length', tilesNumber);

    AdminDashboard
      .pageObject
      .getTiles()
      .should('be.visible')
      .find('p')
      .should('be.visible')
      .should('have.length', tilesNumber);

    AdminDashboard
      .pageObject
      .getTiles()
      .should('be.visible')
      .find('img')
      .should('be.visible')
      .should('have.length', tilesNumber);
  }
}
