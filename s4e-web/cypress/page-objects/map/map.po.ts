import { JwtTokenModal } from './map-jwt-token-modal.po';
import { Core } from './../core.po';
import { User, Login } from '../auth/login.po';
import { SideNav } from '../settings/settings-side-nav.po';
import { MapProducts } from './map-products.po';

export class Map extends Core {
  static pageObject = {
    // TODO: update elements with data-e2e attributes
    getUserBtn: () => cy.get("#user-login-button"),

    // user
    getOpenJwtTokenModalBtn: () => cy.get('[data-e2e="open-jwt-token-btn"]'),
    getSettingsBtn: () => cy.get("a").contains("Ustawienia"),
    getLogoutBtn: () => cy.get("a").contains("Wyloguj"),

    // Date selection
    getDateChangeBtn: () => cy.get(".timeline__changedate"),
    getYearSelectionBtn: () => cy.get(".owl-dt-control-period-button"),
    getYearBtn: (year: number) =>
      cy.get(".owl-dt-calendar-cell-content").contains(year),
    getMonthBtn: (month: number) =>
      cy.get(".owl-dt-calendar-cell-content").eq(month - 1),
    getDayBtn: (day: number) =>
      cy.get(".owl-dt-calendar-cell-content:not(.owl-dt-calendar-cell-out)").contains(day),
    getHourBtn: (hour: number) =>
      cy.get(`.timeline__item[title="${hour < 10 ? '0' + hour : hour}:00"]`, {timeout: 5000}),
    getHourBtnInPopup: (hour: number) =>
      cy.get(`li[data-hour="${hour < 10 ? '0' + hour : hour}:00:00"]`, {timeout: 5000}),
    getStackedHourBtn: (hourStart: number, hourEnd: number) =>
      cy.get(`.timeline__item--multiple[title="${hourStart < 10 ? '0' + hourStart : hourStart}:00 - ${hourEnd < 10 ? '0' + hourEnd : hourEnd}:00"]`,
        {timeout: 5000}),
    getIncreaseResolutionBtn: () =>
      cy.get('.timecontrol__button--plus', {timeout: 5000}),
    getDecreaseResolutionBtn: () =>
      cy.get('.timecontrol__button--minus', {timeout: 5000}),
  };

  static logout() {
    Map.pageObject.getUserBtn().should("be.visible").click({force: true});
    Map.pageObject.getLogoutBtn().should("be.visible").click({force: true});
    cy.location("pathname").should("eq", "/login");

    return Login;
  }

  static goToSettingsAs(user: User) {
    Map.pageObject.getUserBtn().should("be.visible").click();
    Map.pageObject.getSettingsBtn().should("be.visible").click();
    const isAdmin =
      user.email.startsWith("zkAdmin") || user.email.startsWith("admin");
    cy.location("pathname").should(
      "eq",
      isAdmin ? "/settings/dashboard" : "/settings/profile"
    );
    return SideNav;
  }

  static openJwtTokenModal() {
    Map
      .pageObject
      .getUserBtn()
      .should('be.visible')
      .click();
    Map
      .pageObject
      .getOpenJwtTokenModalBtn()
      .should('be.visible')
      .click();

    return JwtTokenModal;
  }

  static openDateChange() {
    Map.pageObject.getDateChangeBtn().should("be.visible").click();
    return Map;
  }

  static selectDate(
    year: number,
    month: number,
    day: number,
  ) {
    Map.pageObject.getYearSelectionBtn().should("be.visible").click();
    Map.pageObject.getYearBtn(year).should("be.visible").click();
    Map.pageObject.getMonthBtn(month).should("be.visible").click();
    Map.pageObject.getDayBtn(day).should("be.visible").click();
    return Map;
  }

  static selectDataPoint(hour: number) {
    Map.pageObject.getHourBtn(hour).should("be.visible").click();
    return Map;
  }

  static selectStackedDataPoint(
    hourStart: number,
    hourEnd: number
  ) {
    cy.wait(500);
    Map.pageObject.getStackedHourBtn(hourStart, hourEnd).should("be.visible").click();
    Map.pageObject.getHourBtnInPopup(hourStart).click();
    return Map;
  }

  static increaseResolution() {
    Map.pageObject.getIncreaseResolutionBtn().click()
    return Map;
  }

  static decreaseResolution() {
    Map.pageObject.getDecreaseResolutionBtn().click()
    return Map;
  }
}
