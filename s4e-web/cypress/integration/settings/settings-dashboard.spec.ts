import { SideNav } from './../../page-objects/settings/settings-side-nav.po';
import { InstitutionSearch } from './../../page-objects/settings/settings-institution-search.po';
import { AdminDashboard, SuperAdminDashboard } from './../../page-objects/settings/settings-dashboard.po';
import { Login } from '../../page-objects/auth/login.po';
context('Dashboard', () => {
  beforeEach(() => {
    cy.fixture('users/zkAdmin.json').as('zkAdmin');
    cy.fixture('users/admin.json').as('admin');
  });

  it('should display admin dashboard', async function () {
    Login
      .loginAs(this.zkAdmin)
      .goToSettingsAs(this.zkAdmin);

    await AdminDashboard
      .tilesShouldContainHeadersDescriptionsAndImages();

    InstitutionSearch
      .selectNthInstitutionResult(0)
      .changeContextTo(AdminDashboard)
      .goToInstitutionProfile();

    SideNav
      .goToDashboardAs(this.zkAdmin)
      .changeContextTo(AdminDashboard)
      .goToPeopleList()
      .changeContextTo(SideNav)
      .logout();
  });
  it('should display super admin dashboard', async function () {
    Login
      .loginAs(this.admin)
      .goToSettingsAs(this.admin);

    await SuperAdminDashboard
      .tilesShouldContainHeadersDescriptionsAndImages();

    SuperAdminDashboard
      .goToInstitutionsList();
  });
});
