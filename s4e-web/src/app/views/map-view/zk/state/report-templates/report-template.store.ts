import { Injectable } from '@angular/core';
import {EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {ReportTemplate, ReportTemplateState} from './report-template.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'ReportTemplates', idKey: 'uuid' })
export class ReportTemplateStore extends EntityStore<ReportTemplateState, ReportTemplate> {
  constructor() {
    super();
  }
}

