import { Core } from './../core.po';


export class MapOptions extends Core {
    static pageObject = {
        getUserBtn: () => cy.get("[data-e2e='zk-options-btn']"),
        getSendMailToExpertBtn: () => cy.get("a").contains("Wsparcie eksperckie"),
        getSupportTypeInput: () => cy.get("#helpType"),
        getTextArea: () => cy.get("#issueDescription"),
        getSendBtn: () => cy.get("button").contains("Wyślij prośbę"),
        getConfirmationMessage: () => cy.get(".message").contains("Prośba o wsparcie eksperckie została wysłana")

    };

    static openSendMailToExpertModal() {
        MapOptions
            .pageObject
            .getUserBtn()
            .click()
        MapOptions
            .pageObject
            .getSendMailToExpertBtn()
            .click()

        return MapOptions
    }

    static addMessageToSupport( select: string, message: string) {
        MapOptions
            .pageObject
            .getSupportTypeInput()
            .select(select)

        MapOptions
            .pageObject
            .getTextArea()
            .type(message)

        return MapOptions
    }

    static sendMessageToSupport() {
        MapOptions
            .pageObject
            .getSendBtn()
            .click()

        return MapOptions
    }

    static confirmationShouldToAppear() {
        MapOptions
            .pageObject
            .getConfirmationMessage()
            .should("be.visible");
    }
}

