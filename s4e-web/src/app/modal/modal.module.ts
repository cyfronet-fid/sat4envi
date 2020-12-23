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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModalOutletComponent} from './components/modal-outlet/modal-outlet.component';
import {DynamicModalComponent} from './components/dynamic-modal/dynamic-modal.component';
import {makeModalProvider} from './modal.providers';
import {DummyModalComponent} from './components/dummy-modal/dummy-modal.component';
import {ModalService} from './state/modal.service';
import {ModalQuery} from './state/modal.query';
import {ModalStore} from './state/modal.store';
import {AlertModalComponent} from './components/alert-modal/alert-modal.component';
import {ConfirmModalComponent} from './components/confirm-modal/confirm-modal.component';
import {GenericModalComponent} from './components/generic-modal/generic-modal.component';
import {ALERT_MODAL_ID} from './components/alert-modal/alert-modal.model';
import {CONFIRM_MODAL_ID} from './components/confirm-modal/confirm-modal.model';
import {DUMMY_MODAL_ID} from './components/dummy-modal/dummy-modal.model';

@NgModule({
  declarations: [
    DynamicModalComponent,
    ModalOutletComponent,
    DummyModalComponent,
    AlertModalComponent,
    ConfirmModalComponent,
    GenericModalComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    ModalOutletComponent,
    GenericModalComponent,
  ],
  providers: [
    makeModalProvider(DUMMY_MODAL_ID, DummyModalComponent),
    makeModalProvider(ALERT_MODAL_ID, AlertModalComponent),
    makeModalProvider(CONFIRM_MODAL_ID, ConfirmModalComponent),
    ModalService,
    ModalQuery,
    ModalStore
  ],
  entryComponents: [
    DummyModalComponent,
    ConfirmModalComponent,
    AlertModalComponent,
  ]
})
export class ModalModule {
}
