/// <reference types = "Cypress" />

import { Registration } from '../../page-objects/auth/auth-register.po';

before(() => {
  cy.fixture("users/userToRegister.json").as("userToRegister");
});

describe('Register', () => {

	beforeEach(() => {
		cy.visit("/register");
	});

	context("Valid form", () => {

		it("shouldn't send empty form", function () {
			Registration
				.sendForm()
				.errorsCountShouldBe(9);
		});

		it("shouldn't send form on incorrect email", function () {
			Registration
				.fillForm({ ...this.userToRegister, email: 'incorrect.pl' })
				.sendForm()
				.errorsCountShouldBe(1);
		});

		it("shouldn't send form on different passwords", function () {
			Registration
				.fillForm({ ...this.userToRegister, repeatPassword: 'incorrectPassword' })
				.sendForm()
				.errorsCountShouldBe(1);
		});
	});

	context('Register user', () => {

		it("should register new user", function () {
     // cy.deleteAllMails();

			Registration
				.fillForm(this.userToRegister)
				.clickReCaptcha()
				.sendForm()
				.beOnConfirmationPage();

      // cy.getAllMails()
      //   .filterBySubject("Potwierdzenie adresu email")
      //   .should('have.length', 1)
      //   .firstMail()
      //   .getMailDocumentContent()
      //   .then(($document: Document) =>
      //     // TODO: Go to activation URL
      //     {
      //       expect(
      //         Array
      //           .from($document.getElementsByTagName('a'))
      //           .map(el => el.href)
      //           .filter((href: string) => href.includes('/activate'))
      //           .length
      //       ).eq(1)
      //     }
      //  );
		});
	});
});
