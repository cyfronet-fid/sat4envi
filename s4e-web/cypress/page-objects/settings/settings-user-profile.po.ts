import { InstitutionProfile } from './settings-institution-profile.po';
import { Core } from './../core.po';
import { UserPasswordChange } from './settings-user-password-change.po';

export class UserProfile extends Core {
  static pageObject = {
    getGoToChangePasswordBtn: () => cy.get('[data-e2e="go-to-password-change"]'),
    getUserDetails: () => cy.get('[data-e2e="user-details"]'),
    getMemberInstitutions: () => cy.get('[data-e2e="member-institutions-tile"]').find('a')
  };

  static goToPasswordChange() {
    return UserProfile.goTo(UserProfile.pageObject.getGoToChangePasswordBtn(), '/settings/change-password', UserPasswordChange);
  }

  static userDetailsShouldContain(...details: string[]) {
    details
      .forEach(detail => {
        UserProfile
          .pageObject
          .getUserDetails()
          .contains(detail)
          .should('be.visible');
      });

    return UserProfile;
  }

  static goToNthMemberInstitution(nth: number) {
    UserProfile
      .pageObject
      .getMemberInstitutions()
      .eq(nth)
      .should('be.visible')
      .click();

    cy.location('pathname').should('eq', '/settings/institution');

    return InstitutionProfile;
  }
}

