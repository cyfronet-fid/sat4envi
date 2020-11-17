import * as Factory from 'factory.ts';
import {ReportTemplate} from './report-template.model';

export const ReportTemplateFactory = Factory.makeFactory<ReportTemplate>({
  uuid: Factory.each(i => `${i}`),
  caption: Factory.each(i => `Report template #${i}`),
  notes: Factory.each(i => `Note #${i}`),
  overlayIds: Factory.each(i => [i]),
  createdAt: null,
  productId: Factory.each(i => i)
});
