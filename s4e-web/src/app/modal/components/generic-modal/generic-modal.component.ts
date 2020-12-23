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
