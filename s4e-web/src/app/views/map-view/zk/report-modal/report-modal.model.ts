import {Modal} from '../../../../modal/state/modal.model';
import {MapData} from '../../state/map/map.model';
import {Legend} from '../../state/legend/legend.model';

export const REPORT_MODAL_ID = 'report-modal';

export interface ReportForm {
  caption: string;
  notes: string;
}

export interface ReportModal extends Modal {
  image: MapData
  sceneDate:  string;
  productName: string;
  legend: Legend|null;
}

export function isReportModal(modal: Modal): modal is ReportModal {
  return modal.id === REPORT_MODAL_ID
    && (modal as ReportModal).image != null
}
