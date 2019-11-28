import {Modal, ModalWithReturnValue} from '../../state/modal.model';

export const CONFIRM_MODAL_ID = 'confirm';

export interface ConfirmModal extends ModalWithReturnValue<boolean> {
  title: string,
  content: string,
}

export function isConfirmModal(obj: Modal): obj is ConfirmModal {
  return obj.id === CONFIRM_MODAL_ID
}
