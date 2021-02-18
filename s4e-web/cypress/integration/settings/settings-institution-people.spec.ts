/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {UserOptionsGoToSettings} from '../../page-objects/user-options/user-options-go-to-settings-profile.po';
import {SettingsInstitutions} from '../../page-objects/settings/settings-institution-profile.po';
import {SettingsInstitutionPeople} from '../../page-objects/settings/settings-institution-people.po';
import {ConfirmModal} from '../../page-objects/modal/confirm-modal.po';
import {SettingsNav} from '../../page-objects/settings/settings-navigation.po';
import {Registration} from '../../page-objects/auth/auth-register.po';
import {SettingsUserDeleteAccount} from '../../page-objects/settings/settings-delete-account.po';
import {UserOptionsAuthentication} from '../../page-objects/user-options/user-option-authentication.po';

before(() => {
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('users/userToRegister.json').as('userToRegister');
});

describe('Inviting people', () => {
  beforeEach(function () {
    cy.server();
    cy.visit('/login');
  });

  context('Accept invitation to institution', () => {
    it('should invite and add to institution un-registered person without administrator privileges', function () {
      cy.deleteAllMails();

      Login.loginAs(this.zkAdmin);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsInstitutionPeople.goToPeopleInInstitutionPage()
        .addInvitation(this.userToRegister.email, false)
        .invitationWithEmailShouldExist(this.userToRegister.email);
      SettingsNav.logOut();

      SettingsInstitutionPeople.clickAcceptJoinToInstitutionLink();

      Login.goToRegisterPage();
      Registration.registerAs(this.userToRegister);

      Registration.clickActivateLink();

      Login.loginAs(this.userToRegister);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsNav.goToUserProfile();
      SettingsUserDeleteAccount.deleteAccount(this.userToRegister.password);
      ConfirmModal.accept();
    });

    it('should invite and add to institution registered person with administrator privileges', function () {
      cy.deleteAllMails();

      Login.goToRegisterPage();
      Registration.registerAs(this.userToRegister);

      Registration.clickActivateLink();

      Login.loginAs(this.userToRegister);
      UserOptionsAuthentication.logout();

      cy.visit('/login');
      Login.loginAs(this.zkAdmin);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsInstitutionPeople.goToPeopleInInstitutionPage().addInvitation(
        this.userToRegister.email,
        true
      );
      SettingsNav.logOut();

      SettingsInstitutionPeople.clickAcceptJoinToInstitutionLink();

      Login.loginAs(this.userToRegister);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsNav.institutionNavigationShouldHaveValue('exist');
      SettingsInstitutions.selectNthInstitution(0).addChildAndEditInstitutionShould(
        'exist'
      );
      SettingsNav.goToUserProfile();
      SettingsUserDeleteAccount.deleteAccount(this.userToRegister.password);
      ConfirmModal.accept();
    });

    it('should invite and add to institution loged in person without administrator privileges', function () {
      cy.deleteAllMails();

      Login.goToRegisterPage();
      Registration.registerAs(this.userToRegister);

      Registration.clickActivateLink();

      Login.loginAs(this.userToRegister);
      UserOptionsAuthentication.logout();

      cy.visit('/login');

      Login.loginAs(this.zkAdmin);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsInstitutionPeople.goToPeopleInInstitutionPage().addInvitation(
        this.userToRegister.email,
        false
      );
      SettingsNav.logOut();
      Login.loginAs(this.userToRegister);

      SettingsInstitutionPeople.clickAcceptJoinToInstitutionLink();

      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsNav.goToUserProfile();
      SettingsUserDeleteAccount.deleteAccount(this.userToRegister.password);
      ConfirmModal.accept();
    });

    it('should invite, re-invite and remove person from the institution', function () {
      cy.deleteAllMails();

      Login.goToRegisterPage();
      Registration.registerAs(this.userToRegister);

      Registration.clickActivateLink();

      Login.loginAs(this.userToRegister);
      UserOptionsAuthentication.logout();

      cy.visit('/login');

      Login.loginAs(this.zkAdmin);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsInstitutionPeople.goToPeopleInInstitutionPage()
        .addInvitation(this.userToRegister.email, false)
        .resendInvitationToEmail(this.userToRegister.email);
      SettingsNav.logOut();

      SettingsInstitutionPeople.clickAcceptJoinToInstitutionLink();

      Login.loginAs(this.userToRegister);
      UserOptionsAuthentication.logout();
      Login.loginAs(this.zkAdmin);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsInstitutionPeople.goToPeopleInInstitutionPage().removeFromInstitution(
        this.userToRegister.email
      );
      ConfirmModal.accept();
      SettingsNav.logOut();
      Login.loginAs(this.userToRegister);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.allInstititutionCountShouldBe(0);
      SettingsNav.goToUserProfile();
      SettingsUserDeleteAccount.deleteAccount(this.userToRegister.password);
      ConfirmModal.accept();
    });
  });

  context('Reject invitation to institution', () => {
    it('should reject invite to institution', function () {
      cy.deleteAllMails();

      Login.goToRegisterPage();
      Registration.registerAs(this.userToRegister);

      Registration.clickActivateLink();
      Login.loginAs(this.userToRegister);
      UserOptionsAuthentication.logout();

      cy.visit('/login');
      Login.loginAs(this.zkAdmin);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.selectNthInstitution(0);
      SettingsInstitutionPeople.goToPeopleInInstitutionPage().addInvitation(
        this.userToRegister.email,
        false
      );
      SettingsNav.logOut();

      SettingsInstitutionPeople.clickRejectJoinToInstitutionLink();
      cy.visit('/login');

      Login.loginAs(this.userToRegister);
      UserOptionsGoToSettings.gotoUserProfile();
      SettingsInstitutions.allInstititutionCountShouldBe(0);
      SettingsNav.goToUserProfile();
      SettingsUserDeleteAccount.deleteAccount(this.userToRegister.password);
      ConfirmModal.accept();
    });
  });

  context('Administrator privileges', () => {
    it('should add and delete administrator privileges person belong to institution', function () {
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
});
