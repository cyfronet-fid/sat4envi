import { ReportTemplateStore } from './report-template.store';

describe('Report template store', () => {
  let store: ReportTemplateStore;

  beforeEach(() => {
    store = new ReportTemplateStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
