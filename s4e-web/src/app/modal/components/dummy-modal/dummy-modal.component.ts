import {Component, Inject} from '@angular/core';
import {ModalComponent} from '../../utils/modal/modal.component';
import {ModalService} from '../../state/modal.service';
import {MODAL_DEF} from '../../modal.providers';
import {Modal} from '../../state/modal.model';

@Component({
  selector: 's4e-dummy-modal',
  templateUrl: './dummy-modal.component.html',
  styleUrls: ['./dummy-modal.component.scss']
})
export class DummyModalComponent extends ModalComponent{
  constructor(modalService: ModalService, @Inject(MODAL_DEF) modal: Modal) {
    super(modalService, modal.id);
  }
}
