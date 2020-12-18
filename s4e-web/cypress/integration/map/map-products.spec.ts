/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { MapDateSelect } from '../../page-objects/map/map-date-select.po';
import { MapProducts } from '../../page-objects/map/map-products.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');  
});

describe('Map Products', () => {

  beforeEach(function(){
    cy.server();
   
    cy.visit('/login');
    Login
      .loginAs(this.zkMember)
  });

  it('should load the product', function(){
    MapProducts
      .selectProductByName('Airmass');
  });

  it('should the live scene be loading', function(){
    MapProducts
    .selectProductByName('Intensywność opadu')
    .turnOnOnLiveView()
    .turnOffOnLiveView();
  });

  it('should load the selected date of the product', function () {
    const year = 2020;
    const month = 2;
    const day = 1;
    
    MapProducts
      .selectProductByName('108m')
    MapDateSelect
      .openDateChange()
      .selectDate(year, month, day);
  });

  it('should dispaly legend', function(){
    MapProducts
    .selectProductByName('Pył w atmosferze')
    .legendShouldBeVisible();
  });
});
