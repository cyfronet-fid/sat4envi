import { Core } from './../core.po';
import { UserPasswordChange } from './settings-user-password-change.po';

export class UserProfile extends Core {
  static pageObject = {
    getGoToChangePasswordBtn: () => cy.get('[data-e2e="go-to-password-change"]'),
    getUserDetails: () => cy.get('[data-e2e="user-details"]')
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
}

