import {Core} from '../core.po';

export class UserOptionsGenerateReport extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getGenerateReportBtn: () => cy.get('[data-e2e="open-generate-report-modal"]'),
    getGenerateTemplateReportBtn: () =>
      cy.get('[data-e2e="open-template-report-modal"]'),
    getGenerateReportModal: () => cy.get('[data-e2e="modal-container"]'),
    getCaptionInput: () => cy.get('[data-e2e="caption"]'),
    getNotesInput: () => cy.get('[data-e2e="notes"]'),
    getSaveBtn: () => cy.get('[data-e2e="btn-submit"]'),
    getSaveAsTemplateBtn: () => cy.get('[data-e2e="submit-as-template-btn"]'),
    getReports: () => cy.get('[data-e2e="report-template-details"]'),
    getLoadReportBtn: () => cy.get('[data-e2e="load-report-btn"]'),
    getDeleteTemplateBtn: () => cy.get('[data-e2e="btn-delete"]')
  };

  static openGenerateReportModal() {
    UserOptionsGenerateReport.pageObject.getOptionsBtn().click();

    UserOptionsGenerateReport.pageObject.getGenerateReportBtn().click();

    return UserOptionsGenerateReport;
  }

  static openGenerateTemplateReportModal() {
    UserOptionsGenerateReport.pageObject.getOptionsBtn().click();

    UserOptionsGenerateReport.pageObject.getGenerateTemplateReportBtn().click();

    return UserOptionsGenerateReport;
  }

  static fillFields(caption: string, notes: string) {
    UserOptionsGenerateReport.pageObject.getCaptionInput().type(caption);

    UserOptionsGenerateReport.pageObject.getNotesInput().type(notes);

    return UserOptionsGenerateReport;
  }

  static saveReportToDisk() {
    UserOptionsGenerateReport.pageObject.getSaveBtn().click();

    return UserOptionsGenerateReport;
  }

  static saveReportAsTemplate() {
    UserOptionsGenerateReport.pageObject.getSaveAsTemplateBtn().click();

    return UserOptionsGenerateReport;
  }

  static reportsCountShouldBe(count: number) {
    UserOptionsGenerateReport.pageObject.getReports().should('have.length', count);

    return UserOptionsGenerateReport;
  }

  static loadNthReport(nth: number) {
    UserOptionsGenerateReport.pageObject.getLoadReportBtn().eq(nth).click();

    UserOptionsGenerateReport.pageObject
      .getGenerateReportModal()
      .should('be.visible');

    return UserOptionsGenerateReport;
  }

  static deleteNthReportTemplate(nth: number) {
    UserOptionsGenerateReport.pageObject.getDeleteTemplateBtn().eq(nth).click();

    return UserOptionsGenerateReport;
  }
}
