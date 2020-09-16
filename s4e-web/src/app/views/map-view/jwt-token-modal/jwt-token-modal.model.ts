import { Modal } from 'src/app/modal/state/modal.model';

export interface JwtTokenForm {
  password: string;
}
export const JWT_TOKEN_MODAL_ID = 'jwt-token-modal';

export function isJwtTokenModal(modal: Modal): modal is Modal {
  return modal.id === JWT_TOKEN_MODAL_ID;
}
