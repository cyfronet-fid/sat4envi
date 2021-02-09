/// <reference types="Cypress" />

import {Login} from '../../page-objects/auth/auth-login.po';
import {UserOptionsGoToSettings} from '../../page-objects/user-options/user-options-go-to-settings-profile.po';
import {SettingsInstitutions} from '../../page-objects/settings/settings-institution-profile.po';
import {SettingsNav} from '../../page-objects/settings/settings-navigation.po';
import {ConfirmModal} from '../../page-objects/modal/confirm-modal.po';
import {SettingsAdministratorPrivilage} from '../../page-objects/settings/settings-superadmin-add-privilege.po';

before(() => {
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('users/admin.json').as('admin');
  cy.fixture('institutions.json').as('institutions');
});
describe('Institution crud', () => {
  before(function () {
    cy.visit('/login');
    Login.loginAs(this.admin);
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsNav.goToManagePrivilege();
    SettingsAdministratorPrivilage.addDeleteInstitutionPrivilege('zkAdmin');
    SettingsNav.logOut();
  });

  beforeEach(function () {
    cy.visit('/login');
    Login.loginAs(this.zkAdmin);
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsNav.goToInstitutionsList();

    SettingsInstitutions.pageObject.getInstitutionsCount();
  });

  it('should add, edit and delete new institution', function () {
    SettingsInstitutions.goToAddNewInstitutionPage()
      .fillForm(this.institutions[5])
      .submit()
      .shouldDisplayInstitutionProfile(this.institutions[5].name);
    SettingsNav.goToInstitutionsList();
    SettingsInstitutions.allInstititutionCountShouldBe(this.institutionsCount + 1)
      .shouldBeOnInstitutionList(this.institutions[5].name)
      .selectInstitutionByName(this.institutions[5].name)
      .goToEditInstitutionPage()
      .editForm('editedCityName', this.institutions[5])
      .submit()
      .editedInstitutionShouldContain('editedCityName');
    SettingsNav.goToInstitutionsList();
    SettingsInstitutions.removeInstitution(this.institutions[5].name);
    ConfirmModal.accept();
    SettingsInstitutions.allInstititutionCountShouldBe(this.institutionsCount);
  });

  it('should add, edit and delete child institution', function () {
    SettingsInstitutions.selectNthInstitution(0)
      .goToAddChildInstitutionPage()
      .fillForm(this.institutions[6], false)
      .submit()
      .shouldDisplayInstitutionProfile(this.institutions[6].name)
      .selectedInstitutionShouldBe(this.institutions[6].name)
      .goToEditInstitutionPage()
      .editForm('editedCityName', this.institutions[6])
      .submit()
      .editedInstitutionShouldContain('editedCityName');
    SettingsNav.goToInstitutionsList();
    SettingsInstitutions.allInstititutionCountShouldBe(this.institutionsCount + 1)
      .shouldBeOnInstitutionList(this.institutions[6].name)
      .selectNthInstitution(0)
      .shouldBeOnInstitutionChildrenList(this.institutions[6].name)
      .selectInstitutionChildByName(this.institutions[6].name)
      .shouldDisplayInstitutionProfile(this.institutions[6].name);
    SettingsNav.goToInstitutionsList();
    SettingsInstitutions.removeInstitution(this.institutions[6].name);
    ConfirmModal.accept();
    SettingsInstitutions.allInstititutionCountShouldBe(this.institutionsCount);
  });

  it("shouldn't add institution without filled fields", function () {
    SettingsInstitutions.goToAddNewInstitutionPage().submit().errorsCountShouldBe(3);
  });

  it('should change institution', function () {
    SettingsInstitutions.selectInstitutionByName(this.institutions[0].name)
      .selectedInstitutionShouldBe(this.institutions[0].name)
      .shouldDisplayInstitutionProfile(this.institutions[0].name);
    SettingsNav.changeInstitution();
    SettingsInstitutions.selectInstitutionByName(this.institutions[1].name)
      .selectedInstitutionShouldBe(this.institutions[1].name)
      .shouldDisplayInstitutionProfile(this.institutions[1].name);
  });

  after(function () {
    SettingsNav.logOut();
    Login.loginAs(this.admin);
    UserOptionsGoToSettings.gotoUserProfile();
    SettingsNav.goToManagePrivilege();
    SettingsAdministratorPrivilage.removeDeleteInstitutionPrivilege('zkAdmin');
    SettingsNav.logOut();
  });
});
