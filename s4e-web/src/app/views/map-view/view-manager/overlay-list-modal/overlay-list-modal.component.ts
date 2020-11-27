import {NotificationService} from 'notifications';
import {Component} from '@angular/core';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {OVERLAY_LIST_MODAL_ID} from './overlay-list-modal.model';
import {OverlayQuery} from '../../state/overlay/overlay.query';
import {OverlayService} from '../../state/overlay/overlay.service';

export interface OverlayForm {
  url: string;
  label: string;
}

@Component({
  selector: 's4e-overlay-list-modal',
  templateUrl: './overlay-list-modal.component.html',
  styleUrls: ['./overlay-list-modal.component.scss']
})
export class OverlayListModalComponent extends ModalComponent {
  constructor(
    modalService: ModalService,
    private _overlayQuery: OverlayQuery,
    private _overlayService: OverlayService,
    private _notificationService: NotificationService
  ) {
    super(modalService, OVERLAY_LIST_MODAL_ID);
  }
}
