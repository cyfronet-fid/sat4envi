import {Component, Inject} from '@angular/core';
import {ModalComponent} from '../../utils/modal/modal.component';
import {ModalService} from '../../state/modal.service';
import {MODAL_DEF} from '../../modal.providers';
import {Modal} from '../../state/modal.model';
import {isConfirmModal} from './confirm-modal.model';

@Component({
  selector: 'nts-confirm-modal',
  templateUrl: './confirm-modal.component.html',
  styleUrls: ['./confirm-modal.component.scss']
})
export class ConfirmModalComponent extends ModalComponent<boolean> {
  title: string = '';
  content: string = '';

  constructor(modalService: ModalService, @Inject(MODAL_DEF) modal: Modal) {
    super(modalService, modal.id);
    if (!isConfirmModal(modal)) {
      throw new Error(`${modal} is not ConfirmModal!`);
    }
    this.title = modal.title;
    this.content = modal.content;
  }

  accept() {
    this.dismiss(true);
  }
}
