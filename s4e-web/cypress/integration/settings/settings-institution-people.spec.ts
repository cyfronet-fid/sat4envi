/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {UserOptionsGoToSettings} from '../../page-objects/user-options/user-options-go-to-settings-profile.po';
import {SettingsInstitutions} from '../../page-objects/settings/settings-institution-profile.po';
import {SettingsInstitutionPeople} from '../../page-objects/settings/settings-institution-people.po';
import {ConfirmModal} from '../../page-objects/modal/confirm-modal.po';
import {SettingsNav} from '../../page-objects/settings/settings-navigation.po';

before(() => {
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Inviting people', () => {
  beforeEach(function () {
    cy.server();
    cy.visit('/login');
    Login.loginAs(this.zkAdmin);
  });

  it('should invite, re-invite and remove person from the institution', function () {
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsInstitutions.selectNthInstitution(0);
    SettingsInstitutionPeople.goToPeopleInInstitutionPage()
      .addInvitation('test@mail.pl')
      .invitationWithEmailShouldExist('test@mail.pl')
      .resendToNewEmail('test@mail.pl')
      .removeFromInstitution('test@mail.pl');
    ConfirmModal.accept();
  });

  it('should add administrator privileges', function () {
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsInstitutions.selectNthInstitution(0);
    SettingsInstitutionPeople.goToPeopleInInstitutionPage().userShouldHaveAdministratorPrivileges(
      'zkMember@mail.pl',
      'not.be.checked'
    );
    SettingsNav.logOut();
    Login.loginAs(this.zkMember);
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsNav.institutionNavigationShouldHaveValue('not.exist');
    SettingsInstitutions.selectNthInstitution(0).addChildAndEditInstitutionShould(
      'not.exist'
    );
    SettingsNav.logOut();
    Login.loginAs(this.zkAdmin);
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsInstitutions.selectNthInstitution(0);
    SettingsInstitutionPeople.goToPeopleInInstitutionPage()
      .addAdministratorPrivileges('zkMember@mail.pl')
      .userShouldHaveAdministratorPrivileges('zkMember@mail.pl', 'be.checked');
    SettingsNav.logOut();
    Login.loginAs(this.zkMember);
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsNav.institutionNavigationShouldHaveValue('exist');
    SettingsInstitutions.selectNthInstitution(0).addChildAndEditInstitutionShould(
      'exist'
    );
    SettingsNav.logOut();
    Login.loginAs(this.zkAdmin);
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsInstitutions.selectNthInstitution(0);
    SettingsInstitutionPeople.goToPeopleInInstitutionPage()
      .deleteAdministratorPrivileges('zkMember@mail.pl')
      .userShouldHaveAdministratorPrivileges('zkMember@mail.pl', 'not.be.checked');
  });
});
