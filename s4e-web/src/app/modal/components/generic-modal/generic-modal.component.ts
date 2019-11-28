import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ModalService} from '../../state/modal.service';

@Component({
  selector: 's4e-generic-modal',
  templateUrl: './generic-modal.component.html',
  styleUrls: ['./generic-modal.component.scss']
})
export class GenericModalComponent {
  @Input() buttonX: boolean;
  @Input() modalId: string|null;

  /**
   * This method will be executed when clicking x on the modal.
   * By default it just calls calls ModalService.hide on the modal
   *
   * If you just want to be notified when modal is being closed use
   * (close) output.
   */
  @Input() xclicked: () => void = () => {
    if(this.modalId != null) {
      this.modalService.hide(this.modalId);
    }
  };

  @Output() close: EventEmitter<any> = new EventEmitter<any>();

  constructor(private modalService: ModalService) { }

  dismiss() {
    this.xclicked();
    this.close.emit();
  }
}
