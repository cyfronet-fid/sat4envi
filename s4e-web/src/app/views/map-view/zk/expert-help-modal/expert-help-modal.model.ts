import { Modal } from 'src/app/modal/state/modal.model';

export interface ExpertHelpForm {
  helpType: string;
  issueDescription: string;
}
export const EXPERT_HELP_MODAL_ID = 'expert-help-modal';

export function isExpertHelpModal(modal: Modal): modal is Modal {
  return modal.id === EXPERT_HELP_MODAL_ID;
}
