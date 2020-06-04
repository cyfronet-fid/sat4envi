import { User } from '../login/login.po';
import { SideNav } from '../settings/side-nav.po';

export namespace Map {
  export class PageObject {
    // TODO: update elements with data-e2e attributes
    static getUserBtn = () => cy.get('#user-login-button');

    // user
    static getSettingsBtn = () => cy.get('a').contains('Ustawienia');
    static getLogoutBtn = () => cy.get('a').contains('Wyloguj');

    // sidebar -> products
    static getNonFavoritesBtns = () => cy.get('.e2e-non-favourite-btn');
    static getFavoritesBtns = () => cy.get('.e2e-favourite-btn');
  }

  export function logout() {
    PageObject
      .getUserBtn()
      .should('be.visible')
      .click();
    PageObject
      .getLogoutBtn()
      .should('be.visible')
      .click();
    cy.location('pathname').should('eq', '/login');
  }

  export function goToSettingsAs(user: User) {
    PageObject
      .getUserBtn()
      .should('be.visible')
      .click();
    PageObject
      .getSettingsBtn()
      .should('be.visible')
      .click();
    const isAdmin = user.email.startsWith('zkAdmin') || user.email.startsWith('admin');
    cy.location('pathname').should('eq', isAdmin ? '/settings/dashboard' : '/settings/profile');
    return SideNav;
  }

  export function selectAllFavorites() {
    PageObject
    .getNonFavoritesBtns()
    .should('be.visible')
    .click({multiple: true, force: true});
  }

  export function unselectAllFavorites() {
    PageObject
      .getFavoritesBtns()
      .should('exist')
      .click({multiple: true, force: true});
  }
}
