/*
 * Copyright 2021 ACC Cyfronet AGH
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
