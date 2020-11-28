import {ActiveState, EntityState} from '@datorama/akita';

export interface ReportTemplate {
  uuid: string;
  caption: string;
  notes: string;
  overlayIds: number[];

  createdAt?: string;
  productId?: number;
}

export interface ReportTemplateState extends EntityState<ReportTemplate>, ActiveState<string> {

}
