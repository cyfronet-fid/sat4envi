// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

import * as unfetch from '../dependencies/unfetch.umd';

// TODO: Remove after Cypress upgrade to version above 6.x.y
/**
 * UnfetchJS library support data (XML) fetching incorrectly working in the cypress 4.x.y
 * and fixed in the latest versions of it
 */
Cypress.on('window:before:load', win => {
  win.eval(unfetch.unfetchFunction)
  win.fetch = win.unfetch
});

/**
 * MAIL HOG COMMANDS
 * IMPORTANT!!! Mailhog encoding doesn't work correctly!!!
 */

// REQUESTS
const MAILHOG_API_V1 = Cypress.env('MAIL_HOG_BASE_URL') + "/" + Cypress.env('MAIL_HOG_API_V1');
Cypress.Commands.add(`deleteAllMails`, () => {
  const url = `${MAILHOG_API_V1}/messages`;
  const method = 'DELETE';
  return cy.wrap(fetch(url, { method }));
});

const MAILHOG_API_V2 = Cypress.env('MAIL_HOG_BASE_URL') + "/" + Cypress.env('MAIL_HOG_API_V2');
function responseParser(responseText) {
  try {
    const json = JSON.parse(
      responseText
        .replace(
          /=[A-Z0-9]{2}/g,
          (substring) => substring.replace('=', '%')
        )
    );

    // Parse response to new format
    function responseMapper(item) {
      return {
        from: `${item.From.Mailbox}@${item.From.Domain}`,
        to: item.To.map(recipient => `${recipient.Mailbox}@${recipient.Domain}`),
        subject: decodeURI(
          item.Content.Headers.Subject[0]
            .replace('=?UTF-8?Q?', '')
            .replace('?=', '')

        )
          .replace('_', ' '),
        content: new DOMParser().parseFromString(unescape(/<!DOCTYPE html>([\s\S]*)<\/html>/gmi.exec(item.Content.Body)[0]), 'text/html')
      };
    }

    return json.items.map(item => responseMapper(item));
  } catch (e) {
    console.error(e);
    return [];
  }
}
Cypress.Commands.add(`getAllMails`, () => {
  const url = `${MAILHOG_API_V2}/messages?limit=9999`;
  const method = 'GET';
  const headers = {
    'Content-Type': 'text/html; charset=UTF-8;',
    'Content-Transfer-Encoding': 'quoted-printable'
  };
  const mode = 'cors';
  return cy.wait(1000)
    .then(() => fetch(url, { method, headers, mode })
      .then(response => response.text())
      .then(responseAsText => responseParser(responseAsText))
    );
});

// FILTERS
Cypress.Commands.add(
  `firstMail`,
  { prevSubject: true },
  (mails$) => cy.wrap(mails$).then(mails => !!mails ? mails[0] : null)
);

Cypress.Commands.add(
  `filterBySubject`,
  { prevSubject: true },
  (mails$, subject) => cy.wrap(mails$)
    .then(mails => mails.filter((mail) => mail.subject.includes(subject)))
);

Cypress.Commands.add(
  `filterByRecipient`,
  { prevSubject: true },
  (mails$, recipient) => cy.wrap(mails$)
    .then(mails => mails.filter((mail) => mail.to.includes(recipient)))
);

Cypress.Commands.add(
  `filterBySender`,
  { prevSubject: true },
  (mails$, from) => cy.wrap(mails$)
    .then(mails => mails.filter((mail) => mail.from.includes(from)))
);

// SINGLE MAIL OPERATIONS
Cypress.Commands.add(
  `getMailSubject`,
  { prevSubject: true },
  (mail) => cy.wrap(mail).its('subject')
);

Cypress.Commands.add(
  `getMailDocumentContent`,
  { prevSubject: true },
  (mail) => cy.wrap(mail).its('content')
);

Cypress.Commands.add(
  `getMailSender`,
  { prevSubject: true },
  (mail) => cy.wrap(mail).its('from')
);

Cypress.Commands.add(
  `getMailRecipients`,
  { prevSubject: true },
  (mail) => cy.wrap(mail).its('to')
);

// Assertions
Cypress.Commands.add(
  'hasMailWithSubject',
  { prevSubject: true },
  (mails$, subject) => cy.wrap(mails$)
    .filterBySubject(subject)
    .should('not.have.length', 0)
);

Cypress.Commands.add(
  'hasMailTo',
  { prevSubject: true },
  (mails$, recipient) => cy.wrap(mails$)
    .filterByRecipient(recipient)
    .should('not.have.length', 0)
);

Cypress.Commands.add("clickActivateLink", () => {
  cy.getAllMails()
    .filterBySubject("Potwierdzenie adresu email")
    .firstMail()
    .getMailDocumentContent()
    .then(($document) => {
      const activateUrl = Array
        .from($document.getElementsByTagName('a'))
        .map(el => el.href)
        .filter((href) => href.includes('/activate')).toString().replace("127.0.0.1", "localhost").replace("=", '');

      cy.visit(activateUrl)
    });
});

Cypress.Commands.add("clickAcceptJoinToInstitutionLink", () => {
  cy.getAllMails()
    .filterBySubject("Zaproszenie")
    .firstMail()
    .getMailDocumentContent()
    .then(($document) => {
      const joinUrl = Array
        .from($document.getElementsByTagName('a'))
        .map(el => el.href)
        .filter((href) => href.includes('login?token=')).toString().replace(/=/g, "")
        .replace("127.0.0.1:4202/login?token", "localhost:4202/login?token=");

      cy.visit(joinUrl)
    });
})

Cypress.Commands.add("clickRejectJoinToInstitutionLink", () => {
  cy.getAllMails()
    .filterBySubject("Zaproszenie")
    .firstMail()
    .getMailDocumentContent()
    .then(($document) => {
      const joinUrl = Array
        .from($document.getElementsByTagName('a'))
        .map(el => el.href)
        .filter((href) => href.includes('login?reject')).toString().replace(/=/g, "")
        .replace("127.0.0.1:4202/login?rejecttrue&token", "localhost:4202/login?reject=true&token=");

      cy.visit(joinUrl)
    });
});

Cypress.Commands.add("clickShareView", () => {
  cy.getAllMails()
    .filterBySubject("Udostępnienie linku")
    .should('have.length', 1)
    .firstMail()
    .getMailDocumentContent()
    .then(($document) =>
    {
      const viewUrl = Array
        .from($document.getElementsByTagName('a'))
        .map(el => el.href)
        .filter((href) => href.includes('/map/products?product')).toString().replace("127.0.0.1", "localhost")

      cy.visit(viewUrl)
    });
});

Cypress.Commands.add("getMailBySubject", (subject) => {
  cy.getAllMails()
    .filterBySubject(subject)
    .should('have.length', 1)
});







