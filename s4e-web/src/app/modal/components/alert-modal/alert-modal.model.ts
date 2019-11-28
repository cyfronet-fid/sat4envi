import {Modal} from '../../state/modal.model';

export const ALERT_MODAL_ID = 'alert';

export interface AlertModal extends Modal {
  title: string,
  content: string,
}

export function isAlertModal(obj: Modal): obj is AlertModal {
  return obj.id === ALERT_MODAL_ID
}
