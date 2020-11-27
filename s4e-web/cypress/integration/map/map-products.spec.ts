/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/login.po';
import { Map } from '../../page-objects/map/map.po';
import { MapProducts } from './../../page-objects/map/map-products.po';

context('Map Products', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
  });

  beforeEach(() => {
    cy.visit('/');
  });


  it('should load product map', function () {
    const year = 2020;
    const month = 2;
    const day = 1;
    Login
      .loginAs(this.zkMember)
      .changeContextTo(MapProducts)
      .selectProductBy('108m')
      .changeContextTo(Map)
      .openDateChange()
      .selectDate(year, month, day)
      .selectStackedDataPointNumber(3, 1)
  });

  // it('should load map by clicking non stacked datapoint', function () {
  //   const year = 2020;
  //   const month = 2;
  //   const day = 1;
  //   const hour = 1;
  //   Login
  //     .loginAs(this.zkMember)
  //     .changeContextTo(MapProducts)
  //     .selectProductBy('108m')
  //     .changeContextTo(Map)
  //     .openDateChange()
  //     .selectDate(year, month, day)
  //     .increaseResolution()
  //     .increaseResolution()
  //     .increaseResolution()
  //     .selectDataPoint(hour)
  // });
});
