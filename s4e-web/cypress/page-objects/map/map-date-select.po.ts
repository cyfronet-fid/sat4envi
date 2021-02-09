import {Core} from '../core.po';

export class MapDateSelect extends Core {
  static readonly pageObject = {
    getDateChangeBtn: () => cy.get('[data-e2e="timeline__changedate"]'),
    getMonthSelectionBtn: () => cy.get('.current').eq(0),
    getYearSelectionBtn: () => cy.get('.current').eq(1),
    getYearBtn: (year: number) => cy.get('[role="gridcell"]').contains(year),
    getMonthBtn: (month: number) => cy.get('[role="gridcell"]').eq(month - 1),
    getDayBtn: (day: number) =>
      cy.get('[role="gridcell"] span:not(.is-other-month)').contains(day),

    getHourBtn: (hour: number) =>
      cy.get(`.timeline__item[title="${hour < 10 ? '0' + hour : hour}:00"]`, {
        timeout: 5000
      }),
    getHourBtnInPopup: (hour: number) =>
      cy.get(`li[data-hour="${hour < 10 ? '0' + hour : hour}:00:00"]`, {
        timeout: 5000
      }),
    getHourBtnInPopupNumber: () => cy.get('.multiple__popup ul li'),
    getStackedHourNumberBtn: () => cy.get('.timeline__item'),
    getStackedHourBtn: (hourStart: number, hourEnd: number) =>
      cy.get(
        `.timeline__item--multiple[title="${
          hourStart < 10 ? '0' + hourStart : hourStart
        }:00 - ${hourEnd < 10 ? '0' + hourEnd : hourEnd}:00"]`,
        {timeout: 5000}
      ),
    getIncreaseResolutionBtn: () =>
      cy.get('.timecontrol__button--plus', {timeout: 5000}),
    getDecreaseResolutionBtn: () =>
      cy.get('.timecontrol__button--minus', {timeout: 5000})
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

  static selectDataPoint(hour: number) {
    MapDateSelect.pageObject.getHourBtn(hour).should('be.visible').click();
    return MapDateSelect;
  }

  static selectStackedDataPoint(hourStart: number, hourEnd: number) {
    MapDateSelect.pageObject
      .getStackedHourBtn(hourStart, hourEnd)
      .should('be.visible')
      .click();
    MapDateSelect.pageObject.getHourBtnInPopup(hourStart).click();
    return MapDateSelect;
  }

  static selectStackedDataPointNumber(stackedHourNumber: number, hour: number) {
    MapDateSelect.pageObject.getStackedHourNumberBtn().eq(stackedHourNumber).click();
    MapDateSelect.pageObject.getHourBtnInPopupNumber().eq(hour).click();
  }

  static increaseResolution() {
    MapDateSelect.pageObject.getIncreaseResolutionBtn().click();
    return MapDateSelect;
  }

  static decreaseResolution() {
    MapDateSelect.pageObject.getDecreaseResolutionBtn().click();
    return MapDateSelect;
  }
}
