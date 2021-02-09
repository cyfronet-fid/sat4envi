export interface User {
  name: string;
  surname: string;
  email: string;
  password: string;
  repeatPassword: string;
  domain: string;
  usage: string;
  country: string;
}

export class Registration {
  static readonly pageObject = {
    getEmailInput: () => cy.get('input[data-e2e="registration-email"]'),
    getNameInput: () => cy.get('input[data-e2e="registration-name"]'),
    getSurnameInput: () => cy.get('input[data-e2e="registration-surname"]'),
    getPasswordInput: () => cy.get('input[data-e2e="registration-password"]'),
    getRepeatPasswordInput: () =>
      cy.get('input[data-e2e="registration-password-repeat"]'),
    getDomaninSelect: () => cy.get('[data-e2e="registration-domain"]'),
    getUsageSelect: () => cy.get('[data-e2e="registration-usage"]'),
    getCountrySelect: () => cy.get('[data-e2e="registration-country"]'),
    getPolicyCheckbox: () => cy.get('[data-e2e="registration-policy"]'),
    getReCaptachaCheckbox: () => cy.get('.recaptcha-checkbox-border'),
    getSubmitBtn: () => cy.get('[data-e2e="btn-submit"]'),

    getErrors: () => cy.get('.invalid-feedback > .ng-star-inserted')
  };

  static fillForm(user: User) {
    Registration.pageObject.getEmailInput().type(user.email);

    Registration.pageObject.getNameInput().type(user.name);

    Registration.pageObject.getSurnameInput().type(user.surname);

    Registration.pageObject.getPasswordInput().type(user.password);

    Registration.pageObject.getRepeatPasswordInput().type(user.repeatPassword);

    Registration.pageObject.getDomaninSelect().select(user.domain);

    Registration.pageObject.getUsageSelect().select(user.usage);

    Registration.pageObject.getCountrySelect().select(user.country);

    Registration.pageObject.getPolicyCheckbox().click();

    return Registration;
  }

  static sendForm() {
    Registration.pageObject.getSubmitBtn().click();

    return Registration;
  }

  static errorsCountShouldBe(count: number) {
    cy.location('pathname').should('eq', '/register');

    Registration.pageObject.getErrors().should('have.length', count);

    return Registration;
  }

  static clickReCaptcha() {
    cy.get('iframe[src*="https://www.google.com/recaptcha"]').then($iframe => {
      const $body = $iframe.contents().find('body');

      cy.wrap($body).find('.recaptcha-checkbox-border').click();

      cy.wrap($body).find('.recaptcha-checkbox-checkmark').should('be.visible');
    });

    cy.wait(1500);

    return Registration;
  }

  static registerAs(user: User) {
    Registration.fillForm(user).clickReCaptcha().sendForm().beOnConfirmationPage();

    return Registration;
  }

  static beOnConfirmationPage() {
    cy.location('pathname').should('eq', '/register-confirmation');

    return Registration;
  }

  static clickActivateLink() {
    cy.getAllMails()
      .filterBySubject('Potwierdzenie adresu email')
      .firstMail()
      .getMailDocumentContent()
      .then(($document: Document) => {
        const activateUrl = Array.from($document.getElementsByTagName('a'))
          .map(el => el.href)
          .filter(href => href.includes('/activate'))
          .toString()
          .replace('=', '');

        cy.visit(activateUrl);
      });
  }
}
