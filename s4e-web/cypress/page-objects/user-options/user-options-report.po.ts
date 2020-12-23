import { Core } from '../core.po';

export class GenerateReport extends Core {
  static pageObject = {
    getOptionsBtn: () => cy.get('[data-e2e="loginOptions-btn"]'),
    getGenerateReportBtn: () => cy.get('[data-e2e="open-generate-report-modal"]'),
    getGenerateTemplateReportBtn: () => cy.get('[data-e2e="open-template-report-modal"]'),
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
    GenerateReport
      .pageObject
      .getOptionsBtn()
      .click();

    GenerateReport
      .pageObject
      .getGenerateReportBtn()
      .click();

    return GenerateReport;
  }

  static openGenerateTemplateReportModal() {
    GenerateReport
      .pageObject
      .getOptionsBtn()
      .click();

    GenerateReport
      .pageObject
      .getGenerateTemplateReportBtn()
      .click();

    return GenerateReport;
  }

  static fillFields(caption: string, notes: string) {
    GenerateReport
      .pageObject
      .getCaptionInput()
      .type(caption);

    GenerateReport
      .pageObject
      .getNotesInput()
      .type(notes);

    return GenerateReport;
  }

  static saveReportToDisk() {
    GenerateReport
      .pageObject
      .getSaveBtn()
      .click();

    return GenerateReport;
  }

  static saveReportAsTemplate() {
    GenerateReport
      .pageObject
      .getSaveAsTemplateBtn()
      .click();

    return GenerateReport;
  }

  static reportsCountShouldBe(count: number) {

    GenerateReport
      .pageObject
      .getReports()
      .should('have.length', count);

    return GenerateReport;
  }

  static loadNthReport(nth: number) {
  
    GenerateReport
      .pageObject
      .getLoadReportBtn()
      .eq(nth)
      .click()

    GenerateReport
      .pageObject
      .getGenerateReportModal()
      .should("be.visible")

    return GenerateReport;
  }

  static deleteNthReportTemplate(nth: number) {
    GenerateReport
      .pageObject
      .getDeleteTemplateBtn()
      .eq(nth)
      .click()

    return GenerateReport;
  };
};