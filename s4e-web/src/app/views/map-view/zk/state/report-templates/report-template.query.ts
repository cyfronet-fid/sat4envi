import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {ReportTemplateStore} from './report-template.store';
import {ReportTemplate, ReportTemplateState} from './report-template.model';

@Injectable({providedIn: 'root'})
export class ReportTemplateQuery extends QueryEntity<ReportTemplateState, ReportTemplate> {
  constructor(protected store: ReportTemplateStore) {
    super(store);
  }
}
