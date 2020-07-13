import {Modal} from '../../../../modal/state/modal.model';

export const INVITATION_FORM_MODAL_ID = 'person-form-modal';

export function isInvitationFormModal(modal: Modal): modal is Modal {
  return modal.id === INVITATION_FORM_MODAL_ID;
}
