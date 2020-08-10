import { User } from "../login/login.po";
import { SideNav } from "../settings/side-nav.po";

export namespace Map {
  export class PageObject {
    // TODO: update elements with data-e2e attributes
    static getUserBtn = () => cy.get("#user-login-button");

    // user
    static getSettingsBtn = () => cy.get("a").contains("Ustawienia");
    static getLogoutBtn = () => cy.get("a").contains("Wyloguj");

    // sidebar -> products
    static getNonFavoritesBtns = () => cy.get(".e2e-non-favourite-btn");
    static getFavoritesBtns = () => cy.get(".e2e-favourite-btn");
    static getFavouriteTab = () => cy.get('[data-e2e="favourite-list"]');
    static getProductsTab = () => cy.get('[data-e2e="product-list"]');
    static getFavouriteCount = () => cy.get('[data-e2e="favourite-count"]');
    static getProducts = () => cy.get(".products-list .products__item");
    static getProductList = () => cy.get(".products-list");
    static getProductsBtns = () =>
      cy.get('s4e-items-picker[caption="Produkty"] .products__name');

    // Date selection
    static getDateChangeBtn = () => cy.get(".timeline__changedate");
    static getYearSelectionBtn = () => cy.get(".owl-dt-control-period-button");
    static getYearBtn = (year: number) =>
      cy.get(".owl-dt-calendar-cell-content").contains(year);
    static getMonthBtn = (month: number) =>
      cy.get(".owl-dt-calendar-cell-content").eq(month - 1);
    static getDayBtn = (day: number) =>
      cy.get(".owl-dt-calendar-cell-content").contains(day);
    static getHourBtn = (hour: number) =>
      cy.get(".timeline__item").contains(hour);
  }

  export function logout() {
    PageObject.getUserBtn().should("be.visible").click();
    PageObject.getLogoutBtn().should("be.visible").click();
    cy.location("pathname").should("eq", "/login");
  }

  export function goToSettingsAs(user: User) {
    PageObject.getUserBtn().should("be.visible").click();
    PageObject.getSettingsBtn().should("be.visible").click();
    const isAdmin =
      user.email.startsWith("zkAdmin") || user.email.startsWith("admin");
    cy.location("pathname").should(
      "eq",
      isAdmin ? "/settings/dashboard" : "/settings/profile"
    );
    return SideNav;
  }

  export function selectProductBy(partialName: string) {
    PageObject.getProductsBtns()
      .contains(partialName)
      .should("be.visible")
      .click({ force: true });
    return Map;
  }

  export function selectNthProduct(number: number) {
    PageObject.getProductsBtns()
      .eq(number)
      .should("be.visible")
      .click({ force: true });
    return Map;
  }

  export function openDateChange() {
    PageObject.getDateChangeBtn().should("be.visible").click();
    return Map;
  }

  export function selectDate(
    year: number,
    month: number,
    day: number,
    hour: number
  ) {
    PageObject.getYearSelectionBtn().should("be.visible").click();
    PageObject.getYearBtn(year).should("be.visible").click();
    PageObject.getMonthBtn(month).should("be.visible").click();
    PageObject.getDayBtn(day).should("be.visible").click();
    cy.wait(500);
    PageObject.getHourBtn(hour).should("be.visible").click();
    return Map;
  }

  export function selectAllFavorites() {
    PageObject.getNonFavoritesBtns()
      .should("be.visible")
      .click({ multiple: true, force: true });

    return Map;
  }

  export function unselectAllFavorites() {
    PageObject.getFavoritesBtns()
      .should("be.visible")
      .click({ multiple: true, force: true });

    return Map;
  }

  export function selectFirstAsFavorite() {
    return PageObject.getNonFavoritesBtns()
      .first()
      .should("exist")
      .click({ force: true });
  }

  export function goToFavourites() {
    PageObject.getFavouriteTab().click({ force: true });
    return Map;
  }

  export function isFavouriteActive() {
    PageObject.getFavouriteTab().should("have.class", "active");
  }
}
