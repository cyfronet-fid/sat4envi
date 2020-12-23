/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
  constructor(modalService: ModalService) {
    super(modalService, OVERLAY_LIST_MODAL_ID);
  }
}
