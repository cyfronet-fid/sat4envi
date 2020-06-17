import { Person } from './../state/person.model';
import {Modal} from '../../../../modal/state/modal.model';

export const PERSON_FORM_MODAL_ID = 'person-form-modal';

export interface PersonFormModal extends Modal {
  person: Person | null;
}

export function isPersonFormModal(modal: Modal): modal is PersonFormModal {
  return modal.id === PERSON_FORM_MODAL_ID;
}
