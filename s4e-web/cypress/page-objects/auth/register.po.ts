
export interface User {
  email: string;
  name: string;
  surname: string;
  password: string;
  repeatPassword: string;
}

export class Registration {
  static readonly pageObject = {
    getEmailInput: () => cy.get("#registration-login"),
    getNameInput: () => cy.get("#registration-name"),
    getSurnameInput: () => cy.get("#registration-surname"),
    getPasswordInput: () => cy.get("#registration-password"),
    getRepeatPasswordInput: () => cy.get("#registration-password-repeat"),
    getReCaptachaCheckbox: () => cy.get(".recaptcha-checkbox-border"),
    getSubmitBtn: () => cy.get('button[type="submit"]'),

    getErrors: () => cy.get('.invalid-feedback > .ng-star-inserted'),

  };

  static fillForm(user: User) {
    Registration.pageObject.getEmailInput().type(user.email);
    Registration.pageObject.getNameInput().type(user.name);
    Registration.pageObject.getSurnameInput().type(user.surname);
    Registration.pageObject.getPasswordInput().type(user.password);
    Registration.pageObject.getRepeatPasswordInput().type(user.repeatPassword);

    return Registration;
  }

  static sendForm() {
    Registration.pageObject.getSubmitBtn().click();

    return Registration;
  }

  static errorsCountShouldBe(count: number) {
    cy.location('pathname').should('eq', '/register');

    Registration
      .pageObject
      .getErrors()
      .should('have.length', count);

    return Registration;
  }

  static clickReCaptcha() {
    cy.get("iframe[src*=\"https://www.google.com/recaptcha\"]")
      .then(($iframe) => {
        const $body = $iframe.contents().find('body')

        cy.wrap($body)
          .find(".recaptcha-checkbox-border")
          .click();

        cy.wrap($body)
          .find(".recaptcha-checkbox-checkmark")
          .should('be.visible');
      });

    cy.wait(1500);

    return Registration;

  }

  static beOnConfirmationPage() {
    cy.location('pathname').should('eq', '/register-confirmation');

    return Registration;
  }
}


