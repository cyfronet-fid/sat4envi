import { ConfirmModal } from './../modal/confirm-modal.po';
import { Core } from '../core.po';

export class InstitutionPeople extends Core {
  static pageObject = {
    // invitations modal
    getOpenSendInvitationBtn: () => cy.get('[data-e2e="open-send-invitation-btn"]'),
    getInvitationEmailInput: () => cy.get('[data-e2e="invitation-email-input"]')
      .find('input'),
    getSubmitFormBtn: () => cy.get('[data-e2e="send-invitation-btn"]'),

    // list
    getParentAndInvitationClass: '[data-e2e="entity-row"]',
    getPeopleAndInvitations: () => cy.get(InstitutionPeople.pageObject.getParentAndInvitationClass),
    getDeleteBtnClass: '[data-e2e="delete-invitation-btn"]',
    getEditBtnClass: '[data-e2e="edit-invitation-btn"]',
    getResendBtnClass: '[data-e2e="resend-invitation-btn"]'
  };

  static fillEmail(email: string) {
    cy.wait(500);

    InstitutionPeople
      .pageObject
      .getInvitationEmailInput()
      .clear()
      .type(email, { force: true });

    return InstitutionPeople;
  }

  static addInvitation(email: string) {
    InstitutionPeople
      .pageObject
      .getOpenSendInvitationBtn()
      .click();

    InstitutionPeople
      .fillEmail(email);

    InstitutionPeople
      .pageObject
      .getSubmitFormBtn()
      .click({ force: true });

    cy.wait(500);

    return InstitutionPeople;
  }

  static invitationsCountShouldBe(count: number) {
    InstitutionPeople
      .pageObject
      .getPeopleAndInvitations()
      .find(InstitutionPeople.pageObject.getResendBtnClass)
      .should('have.length', count);

    InstitutionPeople
      .pageObject
      .getPeopleAndInvitations()
      .find(InstitutionPeople.pageObject.getEditBtnClass)
      .should('have.length', count);

    return InstitutionPeople;
  }

  static invitationWithEmailShouldExist(email: string) {
    InstitutionPeople
      .pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .should('have.length', 1);

    return InstitutionPeople;
  }

  static removeBy(email: string) {
    InstitutionPeople
      .pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .should('have.length', 1)
      .parent()
      .find(InstitutionPeople.pageObject.getDeleteBtnClass)
      .click({ force:  true });

    ConfirmModal.accept();

    return InstitutionPeople;
  }

  static resendToNewEmail(oldEmail: string, newEmail: string) {
    InstitutionPeople
      .pageObject
      .getPeopleAndInvitations()
      .contains(oldEmail)
      .should('have.length', 1)
      .parent()
      .find(InstitutionPeople.pageObject.getEditBtnClass)
      .click({ force: true });

    InstitutionPeople
      .fillEmail(newEmail);

    InstitutionPeople
      .pageObject
      .getSubmitFormBtn()
      .click({ force: true });

    cy.wait(500);

    return InstitutionPeople;
  }
}
