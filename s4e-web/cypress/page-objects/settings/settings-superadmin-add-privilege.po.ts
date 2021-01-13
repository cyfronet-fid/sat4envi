import { Core } from '../core.po';

export class SettingsAdministratorPrivilage extends Core {
  static readonly pageObject = {
    getAdministrator: () => cy.get('[data-e2e="entity-row"]'),
    getDeleteInstitutionPrivilegeCheckbox: '[data-e2e="toggleDeleteAuthority"]'
  };

  static addDeleteInstitutionPrivilege(user: string) {

    SettingsAdministratorPrivilage
      .pageObject
      .getAdministrator()
      .contains(user)
      .parent()
      .find(SettingsAdministratorPrivilage.pageObject.getDeleteInstitutionPrivilegeCheckbox)
      .then(($element) => {
        if ($element.is(':not(:checked)')){
          $element.click()
        }
      })
  }

  static removeDeleteInstitutionPrivilege(user: string) {

    SettingsAdministratorPrivilage
      .pageObject
      .getAdministrator()
      .contains(user)
      .parent()
      .find(SettingsAdministratorPrivilage.pageObject.getDeleteInstitutionPrivilegeCheckbox)
      .then(($element) => {
        if ($element.is(':checked')){
          $element.click()
        }
      })
  }
};
