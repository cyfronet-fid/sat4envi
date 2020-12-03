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

  // TODO: Repair/check seeded values at PR
  // it('should load product map', function () {
  //   const year = 2020;
  //   const month = 2;
  //   const day = 1;
  //   const hourStart = 1;
  //   const hourEnd = 2;
  //   Login
  //     .loginAs(this.zkMember)
  //     .changeContextTo(MapProducts)
  //     .selectProductBy('108m')
  //     .changeContextTo(Map)
  //     .openDateChange()
  //     .selectDate(year, month, day)
  //     .selectStackedDataPoint(hourStart, hourEnd)

  //     // TODO: Replace with filtration of entries

  //   // this is a very fragile hack, we basically give 5 seconds for the request and then check it
  //   // cypress by itself can not intercept wms (image) requests, so thats the only way it can be
  //   // resonably tested at all.
  //   cy.wait(7500).window().then(win => {
  //     const networkrequests = win.performance.getEntries()
  //       .filter(r => /http:\/\/.+\/wms\?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&LAYERS=development%3A108m&TIME=2020-02-01T00%3A00%3A00.000Z&CRS=EPSG%3A3857&.+/.test(r.name));
  //   });
  // });

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
