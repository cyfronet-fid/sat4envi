import {ModalWithReturnValue, Modal} from '../../../../modal/state/modal.model';
import { Institution } from '../../state/institution/institution.model';

export const PARENT_INSTITUTION_MODAL_ID = 'parent-institution-modal';

export interface ParentInstitutionForm {
  searchInstitution: string;
}

export interface ParentInstitutionModal extends ModalWithReturnValue<Institution> {}

export function isParentInstitutionModal(modal: Modal): modal is ParentInstitutionModal {
  return modal.id === PARENT_INSTITUTION_MODAL_ID;
}
