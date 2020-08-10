import { Institution } from 'src/app/views/settings/state/institution/institution.model';
import {Modal} from '../../../../modal/state/modal.model';
import { Invitation } from '../state/invitation/invitation.model';

export const INVITATION_FORM_MODAL_ID = 'person-form-modal';

export interface InvitationFormModal extends Modal {
  institution: Institution;
  invitation: Invitation | null;
}

export function isInvitationFormModal(modal: Modal): modal is Modal {
  return modal.id === INVITATION_FORM_MODAL_ID
    && (modal as InvitationFormModal).institution != null;
}
