/// <reference types = "Cypress" />

import { Registration } from '../../page-objects/auth/register.po'

describe.skip('Register', () => {

    beforeEach(() => {
        cy.visit("/register");
    });

    before(() => {
        cy.fixture("users/userToRegister.json").as("userToRegister");
    })

    context("Valid form", () => {

        it("shouldn't send empty form", function () {
            Registration
                .sendForm()
                .errorsCountShouldBe(5);
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
    })

    context('Register user', () => {

        it("should register new user", function () {

            Registration
                .fillForm(this.userToRegister)
                .clickReCaptcha()
                .sendForm()
                .beOnConfirmationPage()
        });
    });
})

