import { Core } from './../core.po';
import { User, Login } from '../login/login.po';
import { SideNav } from '../settings/settings-side-nav.po';
import { MapProducts } from './map-products.po';

export class Map extends Core {
  static pageObject = {
    // TODO: update elements with data-e2e attributes
    getUserBtn: () => cy.get("#user-login-button"),

    // user
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
      cy.get(".owl-dt-calendar-cell-content").contains(day),
    getHourBtn: (hour: number) =>
      cy.get(".timeline__item").contains(hour),
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

  static openDateChange() {
    Map.pageObject.getDateChangeBtn().should("be.visible").click();
    return Map;
  }

  static selectDate(
    year: number,
    month: number,
    day: number,
    hour: number
  ) {
    Map.pageObject.getYearSelectionBtn().should("be.visible").click();
    Map.pageObject.getYearBtn(year).should("be.visible").click();
    Map.pageObject.getMonthBtn(month).should("be.visible").click();
    Map.pageObject.getDayBtn(day).should("be.visible").click();
    cy.wait(500);
    Map.pageObject.getHourBtn(hour).should("be.visible").click();
    return Map;
  }
}
