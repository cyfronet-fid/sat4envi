import {Modal} from '../../../../modal/state/modal.model';
import { Group } from '../state/group.model';

export const GROUP_FORM_MODAL_ID = 'group-form-modal';

export interface GroupFormModal extends Modal {
  group: Group | null;
}

export function isGroupFormModal(modal: Modal): modal is GroupFormModal {
  return modal.id === GROUP_FORM_MODAL_ID;
}
