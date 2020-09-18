import { SideNav } from './../../page-objects/settings/settings-side-nav.po';
import { AdminDashboard } from './../../page-objects/settings/settings-dashboard.po';
import { Login } from '../../page-objects/login/login.po';
import { Breadcrumbs } from '../../page-objects/settings/settings-breadcrumbs.po';

context('Breadcrumbs', () => {
  beforeEach(() => {
    cy.fixture('users/zkMember.json').as('zkMember');
    cy.fixture('users/zkAdmin.json').as('zkAdmin');
    cy.fixture('users/admin.json').as('admin');
  });

  it('should display admin user profile breadcrumb and go to start page', function () {
    Login
      .loginAs(this.zkAdmin)
      .goToSettingsAs(this.zkAdmin)
      .goToUserProfile()
      .changeContextTo(Breadcrumbs)
      .shouldHaveTexts('Tablica menedżera', 'Mój profil')
      .goToBreadcrumbWithLabel('Tablica menedżera', '/settings/dashboard', AdminDashboard)
      .changeContextTo(SideNav)
      .logout();
  });
  it('should display super admin user profile breadcrumb and go to start page', function () {
    Login
      .loginAs(this.admin)
      .goToSettingsAs(this.admin)
      .goToUserProfile()
      .changeContextTo(Breadcrumbs)
      .shouldHaveTexts('Tablica administratora', 'Mój profil')
      .goToBreadcrumbWithLabel('Tablica administratora', '/settings/dashboard', AdminDashboard)
      .changeContextTo(SideNav)
      .logout();
  });
  it('should display member user profile breadcrumb and go to start page', function () {
    Login
      .loginAs(this.zkMember)
      .goToSettingsAs(this.zkMember)
      .changeContextTo(Breadcrumbs)
      .shouldHaveTexts('Mój profil')
      .changeContextTo(SideNav)
      .logout();
  });
});
