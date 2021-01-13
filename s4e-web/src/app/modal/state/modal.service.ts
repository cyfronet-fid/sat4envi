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

import {Inject, Injectable} from '@angular/core';
import {ModalStore} from './modal.store';
import {createModal, hasReturnValue, Modal, ModalWithReturnValue} from './modal.model';
import {ModalQuery} from './modal.query';
import {DOCUMENT} from '@angular/common';
import {ALERT_MODAL_ID, AlertModal} from '../components/alert-modal/alert-modal.model';
import {map} from 'rxjs/operators';
import {CONFIRM_MODAL_ID, ConfirmModal, isConfirmModal} from '../components/confirm-modal/confirm-modal.model';

@Injectable({providedIn: 'root'})
export class ModalService {

    constructor(private store: ModalStore,
                @Inject(DOCUMENT) private document: Document,
                private query: ModalQuery) {
    }

    hide<T = void>(modalId: string, returnValue?: T) {
        const modal = this.query.getEntity(modalId);
        if (modal == null) {
            return;
        }
        if (hasReturnValue(modal) && returnValue != null) {
            this.store.upsert(modalId, {...modal, returnValue} as ModalWithReturnValue<T>);
        }

        this.store.remove(modalId);
    }

    show<ModalType extends Modal = Modal>(modal: Partial<ModalType> & { id: string }) {
        if (this.document.activeElement != null) {
            (this.document.activeElement as HTMLElement).blur();
        }
        this.store.add(createModal(modal));
    }

    async alert(title: string, content: string): Promise<void> {
        const modalRef = this.show({id: ALERT_MODAL_ID, size: 'sm', content, title} as AlertModal);
        return this.query.modalClosed$(ALERT_MODAL_ID).pipe(map(() => {
        })).toPromise();
    }

    async confirm(title: string, content: string): Promise<boolean> {
        const modalRef = this.show({id: CONFIRM_MODAL_ID, size: 'sm', content, title, hasReturnValue: true} as ConfirmModal);
        return this.query.modalClosed$(CONFIRM_MODAL_ID).pipe(map(m => isConfirmModal(m) ? m.returnValue : false)).toPromise();
    }
}
