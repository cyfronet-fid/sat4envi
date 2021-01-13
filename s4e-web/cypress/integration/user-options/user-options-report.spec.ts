
/// <reference types="Cypress" />

import { Login } from '../../page-objects/auth/auth-login.po';
import { GenerateReport } from "../../page-objects/user-options/user-options-report.po";
import { GeneralModal } from '../../page-objects/modal/general-modal.po';

before(() => {
  cy.fixture('users/zkMember.json').as('zkMember');
});

describe('Generate Report', () => {
  beforeEach(function () {
    cy.visit('/login')

    Login
      .loginAs(this.zkMember)
  });

  it('should save the report', function () {

    GenerateReport
      .openGenerateReportModal()
      .fillFields("test", "test")
      .saveReportToDisk();
  });

  it('should save the report as template', function () {
    GenerateReport
      .openGenerateReportModal()
      .fillFields("test", "test")
      .saveReportAsTemplate()
    GeneralModal
      .closeModal();
    GenerateReport
      .openGenerateTemplateReportModal()
      .reportsCountShouldBe(1)
      .loadNthReport(0);
    GeneralModal
      .cancelModal();
    GenerateReport
      .openGenerateTemplateReportModal()
      .deleteNthReportTemplate(0)
      .reportsCountShouldBe(0);
  })
});