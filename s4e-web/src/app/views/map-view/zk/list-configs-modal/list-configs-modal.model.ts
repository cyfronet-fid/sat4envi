import {Modal} from '../../../../modal/state/modal.model';

export const LIST_CONFIGS_MODAL_ID = 'list-configs-modal';

export interface ListConfigsModal extends Modal {
}


export function isListConfigsModal(modal: Modal): modal is ListConfigsModal {
  return modal.id === LIST_CONFIGS_MODAL_ID;
}
