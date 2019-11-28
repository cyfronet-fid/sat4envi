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
