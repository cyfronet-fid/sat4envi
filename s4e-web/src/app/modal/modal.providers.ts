import {InjectionToken, Provider, Type} from '@angular/core';
import {Modal} from './state/modal.model';

export interface ModalProviderEntry {
  name: string,
  component: Type<any>
}

export const MODAL_DEF = new InjectionToken<Modal>('MODAL_DEF');
export const MODAL_PROVIDER = new InjectionToken<ModalProviderEntry>('MODAL_PROVIDER');
export function makeModalProvider(name: string, component: Type<any>): Provider {
  return {provide: MODAL_PROVIDER, multi: true, useValue: {name, component}};
}
