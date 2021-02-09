import {Core} from '../core.po';

export class SettingsInstitutionPeople extends Core {
  static pageObject = {
    getInstitutionPeopleBtn: () => cy.get('[data-e2e="people"]'),
    getOpenSendInvitationBtn: () => cy.get('[data-e2e="open-send-invitation-btn"]'),
    getInvitationEmailInput: () =>
      cy.get('[data-e2e="invitation-email-input"]').find('input'),
    getSubmitFormBtn: () => cy.get('[data-e2e="send-invitation-btn"]'),
    getPeopleAndInvitations: () => cy.get('[data-e2e="entity-row"]'),
    getSendInvitationModal: () => cy.get('[data-e2e="modal-container"]'),
    getDeleteBtnClass: '[data-e2e="delete-invitation-btn"]',
    getResendBtnClass: '[data-e2e="resend-invitation-btn"]',
    getAdminInvitationBtn: () => cy.get('[data-e2e="adminInvitation"]'),
    getAdminPrivilegeBtn: '[data-e2e="adminPrivilage"]',
    getConfirmationMessage: () => cy.get('.message')
  };

  static goToPeopleInInstitutionPage() {
    SettingsInstitutionPeople.pageObject.getInstitutionPeopleBtn().click();

    cy.location('href').should('include', '/settings/people');

    return SettingsInstitutionPeople;
  }

  static fillEmail(email: string) {
    SettingsInstitutionPeople.pageObject
      .getInvitationEmailInput()
      .clear()
      .type(email);

    return SettingsInstitutionPeople;
  }

  static addInvitation(email: string, administrator: boolean) {
    cy.server();
    cy.route('POST', '/api/v1/institutions/*/invitations').as('sendInvitation');

    SettingsInstitutionPeople.pageObject.getOpenSendInvitationBtn().click();

    SettingsInstitutionPeople.pageObject
      .getSendInvitationModal()
      .should('be.visible');

    SettingsInstitutionPeople.fillEmail(email);

    if (administrator) {
      SettingsInstitutionPeople.pageObject.getAdminInvitationBtn().click();
    }

    SettingsInstitutionPeople.pageObject.getSubmitFormBtn().click();

    cy.wait('@sendInvitation');

    return SettingsInstitutionPeople;
  }

  static invitationsCountShouldBe(count: number) {
    SettingsInstitutionPeople.pageObject
      .getPeopleAndInvitations()
      .find(SettingsInstitutionPeople.pageObject.getResendBtnClass)
      .should('have.length', count);

    return SettingsInstitutionPeople;
  }

  static invitationWithEmailShouldExist(email: string) {
    SettingsInstitutionPeople.pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .should('have.length', 1);

    return SettingsInstitutionPeople;
  }

  static removeFromInstitution(email: string) {
    SettingsInstitutionPeople.pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .parent()
      .find(SettingsInstitutionPeople.pageObject.getDeleteBtnClass)
      .click();

    return SettingsInstitutionPeople;
  }

  static resendInvitationToEmail(email: string) {
    cy.server();
    cy.route('PUT', '/api/v1/institutions/*/invitations').as('sendInvitation');

    SettingsInstitutionPeople.pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .parent()
      .find(SettingsInstitutionPeople.pageObject.getResendBtnClass)
      .click();

    SettingsInstitutionPeople.pageObject
      .getConfirmationMessage()
      .should('be.visible');

    cy.wait('@sendInvitation');

    return SettingsInstitutionPeople;
  }

  static addAdministratorPrivileges(email: string) {
    cy.server();
    cy.route('POST', '/api/v1/institutions/*/admins/*').as(
      'addAdministratorPrivileges'
    );

    SettingsInstitutionPeople.pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .parent()
      .find(SettingsInstitutionPeople.pageObject.getAdminPrivilegeBtn)
      .click();

    SettingsInstitutionPeople.pageObject
      .getConfirmationMessage()
      .should('be.visible');

    cy.wait('@addAdministratorPrivileges');

    return SettingsInstitutionPeople;
  }
  static deleteAdministratorPrivileges(email: string) {
    cy.server();
    cy.route('DELETE', '/api/v1/institutions/*/admins/*').as(
      'deleteAdministratorPrivileges'
    );

    SettingsInstitutionPeople.pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .parent()
      .find(SettingsInstitutionPeople.pageObject.getAdminPrivilegeBtn)
      .click();

    SettingsInstitutionPeople.pageObject
      .getConfirmationMessage()
      .should('be.visible');

    cy.wait('@deleteAdministratorPrivileges');

    return SettingsInstitutionPeople;
  }

  static userShouldHaveAdministratorPrivileges(email: string, value: string) {
    SettingsInstitutionPeople.pageObject
      .getPeopleAndInvitations()
      .contains(email)
      .parent()
      .find(SettingsInstitutionPeople.pageObject.getAdminPrivilegeBtn)
      .should(value);

    return SettingsInstitutionPeople;
  }

  static clickAcceptJoinToInstitutionLink() {
    cy.getAllMails()
      .filterBySubject('Zaproszenie')
      .firstMail()
      .getMailDocumentContent()
      .then(($document: Document) => {
        const joinUrl = Array.from($document.getElementsByTagName('a'))
          .map(el => el.href)
          .filter(href => href.includes('login?token='))
          .toString()
          .replace(/=/g, '')
          .replace('/login?token', '/login?token=');

        cy.visit(joinUrl);
      });
  }

  static clickRejectJoinToInstitutionLink() {
    cy.getAllMails()
      .filterBySubject('Zaproszenie')
      .firstMail()
      .getMailDocumentContent()
      .then(($document: Document) => {
        const joinUrl = Array.from($document.getElementsByTagName('a'))
          .map(el => el.href)
          .filter(href => href.includes('login?reject'))
          .toString()
          .replace(/=/g, '')
          .replace('/login?rejecttrue&token', '/login?reject=true&token=');

        cy.visit(joinUrl);
      });
  }
}
