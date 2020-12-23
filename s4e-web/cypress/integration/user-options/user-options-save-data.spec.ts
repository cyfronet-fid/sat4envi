
/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { SaveData } from '../../page-objects/user-options/user-options-save-data.po';
import { MapProducts } from '../../page-objects/map/map-products.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Save image and data', () => {
  beforeEach(function () {
    cy.server()
    cy.visit('/login')

    Login
      .loginAs(this.zkMember)
  });

  it('should save png image', function () {
    SaveData
      .saveImageOnDisk();
  });

  it('should open modal with metadata and raw data', function () {
    MapProducts
      .selectProductByName('Intensywność opadu')
    SaveData
      .openSaveMetaDataOnDiskModal()
  });
});