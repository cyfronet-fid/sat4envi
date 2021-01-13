/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { UserOptionsGoToSettings } from "../../page-objects/user-options/user-options-go-to-settings-profile.po";
import { SettingsInstitutions } from "../../page-objects/settings/settings-institution-profile.po";
import { SettingsNav } from "../../page-objects/settings/settings-navigation.po";
import { SettingsManageProducts } from "../../page-objects/settings/settings-manage-products.po"
import { MapProducts } from "../../page-objects/map/map-products.po"

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('users/cat1user.json').as('cat1user');
});

describe('Manage products', () => {

  beforeEach(function () {
    cy.visit('/login');
    Login
      .loginAs(this.zkAdmin);
    cy.server();
  });

  it('should displayed institution products', function () {
    MapProducts
      .selectProductByName("dobowy")
      .selectProductByName("godzinny");
    UserOptionsGoToSettings
      .gotoUserProfile();
    SettingsNav
      .goToInstitutionsList();
    SettingsInstitutions
      .selectNthInstitution(0);
    SettingsManageProducts
      .goToManageProductsPage()
      .productsCountShouldBe(2);
    SettingsNav
      .logOut()
    Login
      .loginAs(this.cat1user)
    MapProducts
      .productWithNameShouldNotBeVisible("dobowy")
      .productWithNameShouldNotBeVisible("godzinny")
  });
});



