import {Modal} from '../../../../modal/state/modal.model';

export const REPORT_MODAL_ID = 'report-modal';

export interface ReportForm {
  caption: string;
  notes: string;
}

export interface ReportModal extends Modal {
  mapImage: string;
  mapWidth: number;
  mapHeight: number;
  sceneDate:  string;
  productName: string;
}

export function isReportModal(modal: Modal): modal is ReportModal {
  return modal.id === REPORT_MODAL_ID
    && (modal as ReportModal).mapImage != null
    && (modal as ReportModal).mapWidth != null
    && (modal as ReportModal).mapHeight != null;
}
