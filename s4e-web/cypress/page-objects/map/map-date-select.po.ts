import {Core} from '../core.po';
import {MapProducts} from './map-products.po';

export class MapDateSelect extends Core {
  static readonly pageObject = {
    getDateChangeBtn: () => cy.get('[data-e2e="timeline__changedate"]'),
    getMonthSelectionBtn: () => cy.get('.current').eq(0),
    getYearSelectionBtn: () => cy.get('.current').eq(1),
    getYearBtn: (year: number) => cy.get('[role="gridcell"]').contains(year),
    getMonthBtn: (month: number) => cy.get('[role="gridcell"]').eq(month - 1),
    getDayBtn: (day: number) =>
      cy.get('[role="gridcell"] span:not(.is-other-month)').contains(day),

    getTimeLineNoScenes: () => cy.get('.timeline__item--noproduct'),
    getTimeLineHoursPointsBtn: () =>
      cy.get('.timeline__item div:not(.timeline__item--noproduct)'),

    getMultipleHourPointBtn: (hourStart: number, hourEnd: number) =>
      cy.get(
        `.timeline__item--multiple[title="${
          hourStart < 10 ? '0' + hourStart : hourStart
        }:00 - ${hourEnd < 10 ? '0' + hourEnd : hourEnd}:00"]`,
        {timeout: 5000}
      ),

    getSingleHourPointBtn: (hour: number) =>
      cy.get(`.timeline__item[title="${hour < 10 ? '0' + hour : hour}:00"]`, {
        timeout: 5000
      }),

    getNumberHoursBtnInOpenListFromMultipleDot: () =>
      cy.get('.multiple__popup ul li'),

    getHourBtnInOpenListFromMultipleDot: (hour: number) =>
      cy.get(`li[data-hour="${hour < 10 ? '0' + hour : hour}:00:00"]`, {
        timeout: 5000
      }),

    getIncreaseResolutionBtn: () =>
      cy.get('.timecontrol__button--plus', {timeout: 5000}),
    getDecreaseResolutionBtn: () =>
      cy.get('.timecontrol__button--minus', {timeout: 5000}),

    getHourBtnMobile: () => cy.get('.timecontrol--scenemobile'),

    getTimeLineHourModalMobile: () => cy.get('[data-e2e="mobile-scene-modal"]'),

    getHourInTimeLineHourModalMobile: (hour: number) =>
      cy.get(
        `[data-e2e="mobile-scene-timestamp"][title="${
          hour < 10 ? '0' + hour : hour
        }:00"]`
      )
  };

  static openDateChange() {
    MapDateSelect.pageObject.getDateChangeBtn().should('be.visible').click();
    return MapDateSelect;
  }

  static selectDate(year: number, month: number, day: number) {
    cy.server();
    cy.route(
      'GET',
      `/api/v1/products/*/scenes?date=${year}-${month < 10 ? '0' + month : month}-${
        day < 10 ? '0' + day : day
      }{*,*/*}`
    ).as('loadedProduct');

    MapDateSelect.pageObject.getYearSelectionBtn().should('be.visible').click();
    MapDateSelect.pageObject.getYearBtn(year).should('be.visible').click();
    MapDateSelect.pageObject.getMonthBtn(month).should('be.visible').click();
    MapDateSelect.pageObject.getDayBtn(day).should('be.visible').click();

    cy.wait('@loadedProduct');

    return MapDateSelect;
  }

  static selectHour(hour: number) {
    MapDateSelect.pageObject
      .getSingleHourPointBtn(hour)
      .should('be.visible')
      .click();
    return MapDateSelect;
  }

  static selectHourFromStackedPoint(hourStart: number, hourEnd: number) {
    MapDateSelect.pageObject
      .getMultipleHourPointBtn(hourStart, hourEnd)
      .should('be.visible')
      .click();
    MapDateSelect.pageObject.getHourBtnInOpenListFromMultipleDot(hourStart).click();

    return MapDateSelect;
  }

  static selectHourNumberFromStackedPoint(stackedHourNumber: number, hour: number) {
    MapDateSelect.pageObject.getTimeLineHoursPointsBtn().should('be.visible');
    MapDateSelect.pageObject
      .getTimeLineHoursPointsBtn()
      .eq(stackedHourNumber)
      .click();
    MapDateSelect.pageObject
      .getNumberHoursBtnInOpenListFromMultipleDot()
      .eq(hour)
      .click();

    return MapDateSelect;
  }

  static increaseResolution() {
    MapDateSelect.pageObject.getIncreaseResolutionBtn().click();
    return MapDateSelect;
  }

  static decreaseResolution() {
    MapDateSelect.pageObject.getDecreaseResolutionBtn().click();
    return MapDateSelect;
  }

  static hoursSelectionLineShouldNotDisplayed() {
    MapDateSelect.pageObject.getTimeLineNoScenes().should('be.visible');

    return MapDateSelect;
  }

  static selectHourForMobile(hour: number) {
    MapDateSelect.pageObject.getHourBtnMobile().click();
    MapDateSelect.pageObject
      .getHourInTimeLineHourModalMobile(hour)
      .should('be.visible')
      .click({force: true});
    MapDateSelect.pageObject.getTimeLineHourModalMobile().should('not.exist');

    return MapDateSelect;
  }

  static hourSelectionShouldBeDisabledMobile() {
    MapDateSelect.pageObject.getHourBtnMobile().should('be.disabled');

    return MapDateSelect;
  }
}
