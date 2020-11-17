import { Injectable } from '@angular/core';
import {EntityState, EntityStore, StoreConfig} from '@datorama/akita';
import {ReportTemplate} from './report-template.model';

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'ReportTemplates', idKey: 'uuid' })
export class ReportTemplateStore extends EntityStore<EntityState<ReportTemplate>, ReportTemplate> {
  constructor() {
    super();
  }
}

