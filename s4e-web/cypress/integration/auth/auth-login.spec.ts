/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
  cy.fixture('users/zkAdmin.json').as('zkAdmin');
  cy.fixture('users/admin.json').as('superAdmin');
  cy.fixture('users/userToRegister.json').as('userToRegister');
});

describe('Auth', () => {

	beforeEach(() => {
		cy.visit("/login");
	});

	context('Auth login', () => {

		it('should login as superAdmin', function () {
			Login
				.loginAs(this.superAdmin)
				.logout();
		});

		it('should login as zkMember', function () {
			Login
				.loginAs(this.zkMember)
				.logout();
		});
		it('should login as zkAdmin', function () {
			Login
				.loginAs(this.zkAdmin)
				.logout();
		});
	});

	context('Valid form', () => {

		it("shouldn't send empty form", function () {
			Login
				.unfillForm()
				.errorsCountShouldBe(2);
		});

		it("shouldn't login not-registered user", function () {
			Login
				.fillForm(this.userToRegister)
				.sendForm()
				.hasErrorLogin();
		});

		it("should't login user when email have wrong format", function () {
			Login
				.fillForm({ ...this.userToRegister, email: 'wrongFormat' })
				.errorsCountShouldBe(1);
		});

		it('should not allow login page for user with session', function () {
			Login
				.loginAs(this.superAdmin)
				.loginPageShouldNotBeAllowed()

		});
	});
});