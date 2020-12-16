import { Core } from '../core.po';

export interface User {
	email: string;
	password: string;
}

export class Login extends Core {
	
	static readonly pageObject = {
		getLoginInput: () => cy.get('input[data-e2e="login-email-input"]'),
		getPasswordInput: () => cy.get('input[data-e2e="login-password-input"]'),
		getSubmitBtn: () => cy.get('button[data-e2e="login-submit-btn"]'),
		getGoToMapBtn: () => cy.get('a[data-e2e="go-to-map-btn"]'),
		getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
		getLoginOutBtn: () => cy.get('[data-e2e="logout-btn"]'),
		getOptionsDropdown: () => cy.get('[data-e2e="options-dropdown"]'),

		getFieldErrors: () => cy.get('.invalid-feedback > .ng-star-inserted'),
		getError: () => cy.get('.message')
	};

	static fillForm(user: User) {
		Login
			.pageObject
			.getLoginInput()
			.type(user.email);
		Login
			.pageObject
			.getPasswordInput()
			.type(user.password);

		return Login;
	};

	static unfillForm() {
		Login
		  .pageObject
		  .getSubmitBtn()
		  .invoke("removeAttr","disabled")
		  .click()

		return Login;
	};

	static sendForm() {
		Login
			.pageObject
			.getSubmitBtn()
			.click();

		return Login;
	};


	static errorsCountShouldBe(count: number) {
		cy.location('pathname').should('eq', '/login');

		Login
			.pageObject
			.getFieldErrors()
			.should('have.length', count);

		return Login;
	};

	static hasErrorLogin() {
		cy.location('pathname').should('eq', '/login');

		Login
			.pageObject
			.getError();

		return Login;
	};

	static loginAs(user: User) {

		Login
			.fillForm(user)
			.sendForm();

		cy.location('href').should('include', '/map/products?');

		return Login;
	};

	static logout() {

		Login.
			pageObject
			.getOptionsBtn()
			.should("be.visible")
			.click()

		Login
			.pageObject
			.getOptionsDropdown()
			.should("be.visible")

		Login
			.pageObject
			.getLoginOutBtn()
			.click()

		cy.location('pathname').should('eq', '/login')

		return Login;
	};

	static loginPageShouldNotBeAllowed() {
		cy.visit('/login')
		cy.url().should('not.contain', '/login');

		return Login;
	};

	static goToMapWithoutLogin(){

		Login
			.pageObject
			.getGoToMapBtn()
			.click()

			cy.location('href').should('include', '/map/products?');
	};
};