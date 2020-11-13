import { Injectable } from '@angular/core';
import {EntityState, QueryEntity} from '@datorama/akita';
import { ReportTemplateStore } from './report-template.store';
import {ReportTemplate} from './report-template.model';

@Injectable({ providedIn: 'root' })
export class ReportTemplateQuery extends QueryEntity<EntityState<ReportTemplate>, ReportTemplate> {
  constructor(protected store: ReportTemplateStore) {
    super(store);
  }
}
