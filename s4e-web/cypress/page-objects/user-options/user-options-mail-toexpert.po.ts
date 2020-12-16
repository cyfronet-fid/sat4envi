import { Core } from '../core.po';


export class MailToExpert extends Core {
    static pageObject = {
        getUserBtn: () => cy.get("[data-e2e='zk-options-btn']"),
        getSendMailToExpertBtn: () => cy.get("a").contains("Wsparcie eksperckie"),
        getSupportTypeInput: () => cy.get("#helpType"),
        getTextArea: () => cy.get("#issueDescription"),
        getSendBtn: () => cy.get("button").contains("Wyślij prośbę"),
        getConfirmationMessage: () => cy.get(".message").contains("Prośba o wsparcie eksperckie została wysłana")

    };

    static openSendMailToExpertModal() {
        MailToExpert 
            .pageObject
            .getUserBtn()
            .click()
            MailToExpert 
            .pageObject
            .getSendMailToExpertBtn()
            .click()

        return MailToExpert;
    }

    static addMessageToSupport( select: string, message: string) {
        MailToExpert 
            .pageObject
            .getSupportTypeInput()
            .select(select)

            MailToExpert 
            .pageObject
            .getTextArea()
            .type(message)

        return MailToExpert;
    };

    static sendMessageToSupport() {
        MailToExpert 
            .pageObject
            .getSendBtn()
            .click()

        return MailToExpert;
    };

    static confirmationShouldToAppear() {
        MailToExpert 
            .pageObject
            .getConfirmationMessage()
            .should("be.visible");

        return MailToExpert;
    };
};