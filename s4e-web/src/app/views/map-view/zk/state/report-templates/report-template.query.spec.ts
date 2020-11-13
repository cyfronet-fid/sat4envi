import { ReportTemplateQuery } from './report-template.query';
import { ReportTemplateStore } from './report-template.store';

describe('Report template query', () => {
  let query: ReportTemplateQuery;

  beforeEach(() => {
    query = new ReportTemplateQuery(new ReportTemplateStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
