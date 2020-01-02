import {Modal} from '../../../../modal/state/modal.model';
import {ViewConfigurationEx} from '../../state/view-configuration/view-configuration.model';

export const SAVE_CONFIG_MODAL_ID = 'save-config-modal';

export interface SaveConfigForm {
  configurationName: string;
}

export interface SaveConfigModal extends Modal {
  viewConfiguration : ViewConfigurationEx
}

export function isSaveConfigModal(modal: Modal): modal is SaveConfigModal {
  return modal.id == SAVE_CONFIG_MODAL_ID
    && (modal as SaveConfigModal).viewConfiguration != null;
}
