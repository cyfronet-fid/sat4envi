import {Modal} from '../../state/modal.model';

export function assertModalType<T extends Modal>(assertFunction: (modal: Modal) => modal is T, modal: Modal): modal is T {
  if (!assertFunction(modal)) {
    throw new Error(`${modal} is not a valid! Expected type differs (${assertFunction.name})`);
  }
  return true;
}
