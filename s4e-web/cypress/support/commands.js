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
})

