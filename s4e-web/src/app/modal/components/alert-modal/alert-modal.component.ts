import {Component, Inject} from '@angular/core';
import {ModalComponent} from '../../utils/modal/modal.component';
import {ModalService} from '../../state/modal.service';
import {MODAL_DEF} from '../../modal.providers';
import {Modal} from '../../state/modal.model';
import {isAlertModal} from './alert-modal.model';

@Component({
  selector: 's4e-alert-modal',
  templateUrl: './alert-modal.component.html',
  styleUrls: ['./alert-modal.component.scss']
})
export class AlertModalComponent extends ModalComponent {
  title: string = '';
  content: string = '';

  constructor(modalService: ModalService, @Inject(MODAL_DEF) modal: Modal) {
    super(modalService, modal.id);
    if (!isAlertModal(modal)) {
      throw new Error(`${modal} is not AlertModal!`);
    }
    this.title = modal.title;
    this.content = modal.content;
  }
}
