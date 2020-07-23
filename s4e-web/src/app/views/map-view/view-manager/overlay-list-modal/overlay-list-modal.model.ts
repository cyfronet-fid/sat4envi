import {Modal} from '../../../../modal/state/modal.model';

export const OVERLAY_LIST_MODAL_ID = 'overlay-list-modal-id';

export interface OverlayListModal extends Modal {
}


export function isOverlayListModal(modal: Modal): modal is OverlayListModal {
  return modal.id === OVERLAY_LIST_MODAL_ID;
}
