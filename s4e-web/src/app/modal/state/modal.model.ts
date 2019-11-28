import {EntityState, guid} from '@datorama/akita';

export type ModalSize = 'sm'|'md'|'lg'|'fl';

export interface Modal {
  id: string;
  uuid: string;
  size: ModalSize;
}

export interface ModalWithReturnValue<T> extends Modal{
  returnValue: T;
  hasReturnValue: true;
}

export function hasReturnValue<T>(value: Modal): value is ModalWithReturnValue<T> {
  return (value as any).hasReturnValue == true;
}

export interface ModalState extends EntityState<Modal, string> {}

/**
 * A factory function that creates Modal
 */
export function createModal<T>(params: Partial<Modal> & {id: string}): Modal {
  return {
    uuid: guid(),
    size: 'md',
    ...params
  };
}
