import { AdminDashboard, SuperAdminDashboard } from './settings-dashboard.po';
import { User } from './../login/login.po';
import { InstitutionSearch } from './settings-institution-search.po';
import { InstitutionProfile } from './settings-institution-profile.po';
import { Login } from '../login/login.po';
import {InstitutionList} from './settings-institution-list.po';
import { Core } from '../core.po';
import { AddInstitutionForm } from './settings-add-institution-form.po';
import { InstitutionPeople } from './settings-institution-people.po';
import { UserProfile } from './settings-user-profile.po';

export class SideNav extends Core {
  static pageObject = {
    // TODO: update elements with data-e2e attributes
    getAddInstitutionBtn: () => cy.get('[data-e2e="addInstitution"]'),
    getProfileBtn: () => cy.get('li[data-e2e="profile"] a'),
    getGoToDashboardBtn: () => cy.get('[data-e2e="go-to-dashboard"]').find('a'),
    getLogoutBtn: () => cy.get('.login a'),
    getInstitutionListBtn: () => cy.get('li[data-e2e="institutions"] a'),
    getInstitutionProfileBtn: () => cy.get('[data-e2e="go-to-institution-profile-btn"]'),
    getInstitutionPeopleBtn: () => cy.get('[data-e2e="go-to-institution-people-btn"]')
  };

  static goToNthInstitutionProfile(nth: number) {
    InstitutionSearch
      .selectNthInstitutionResult(nth)
      .changeContextTo(SideNav)
      .goToInstitutionProfile();

    return InstitutionProfile;
  }

  static goToNthInstitutionPeople(nth: number) {
    InstitutionSearch
      .selectNthInstitutionResult(nth)
      .changeContextTo(SideNav)
      .goToInstitutionPeople();

    return InstitutionPeople;
  }

  static goToInstitutionProfile() {
    return SideNav.goTo(SideNav.pageObject.getInstitutionProfileBtn(), '/settings/institution', InstitutionProfile);
  }

  static goToAddInstitution() {
    return SideNav.goTo(SideNav.pageObject.getAddInstitutionBtn(), '/settings/add-institution', AddInstitutionForm);
  }

  static goToUserProfile() {
    return SideNav.goTo(SideNav.pageObject.getProfileBtn(), '/settings/profile', UserProfile);
  }

  static logout() {
    return SideNav.goTo(SideNav.pageObject.getLogoutBtn(), '/login', Login);
  }

  static goToInstitutionList() {
    return SideNav.goTo(SideNav.pageObject.getInstitutionListBtn(), '/settings/institutions', InstitutionList);
  }

  static goToInstitutionPeople() {
    return SideNav.goTo(SideNav.pageObject.getInstitutionPeopleBtn(), '/settings/people', InstitutionPeople);
  }

  static goToDashboardAs(user: User) {
    const isAdmin = user.email.startsWith('zkAdmin');
    const actualContext = isAdmin ? AdminDashboard : SuperAdminDashboard;
    return SideNav.goTo(SideNav.pageObject.getGoToDashboardBtn(), '/settings/dashboard', actualContext);
  }
}
