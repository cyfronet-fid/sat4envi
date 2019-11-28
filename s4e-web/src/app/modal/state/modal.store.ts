import {Injectable} from '@angular/core';
import {EntityStore, StoreConfig} from '@datorama/akita';
import {Modal, ModalState} from './modal.model';

@Injectable({providedIn: 'root'})
@StoreConfig({name: 'Modal'})
export class ModalStore extends EntityStore<ModalState, Modal> {
  constructor() {
    super();
  }
}

