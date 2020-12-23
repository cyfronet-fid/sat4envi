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

import {OnDestroy, OnInit} from '@angular/core';
import {ModalService} from '../../state/modal.service';
import {FormGroup} from '@ng-stack/forms';
import {environment} from '../../../../environments/environment';
import {devRestoreFormState} from '../../../utils/miscellaneous/miscellaneous';
import {FormState} from '../../../state/form/form.model';
import {Observable} from 'rxjs';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {ModalQuery} from '../../state/modal.query';

export class ModalComponent<ReturnType = void> {
  constructor(protected modalService: ModalService, public registeredId?: string) {
  }

  public dismiss(returnValue?: ReturnType): void {
    this.modalService.hide(this.registeredId, returnValue);
  }
}

export abstract class FormModalComponent<FormKey extends keyof FormState, ReturnType = void> extends ModalComponent<ReturnType> implements OnInit, OnDestroy {
  formKey: FormKey;
  form: FormGroup<FormState[FormKey]>;
  isLoading$: Observable<boolean>;

  protected constructor(protected fm: AkitaNgFormsManager<FormState>,
                        protected modalService: ModalService,
                        protected modalQuery: ModalQuery,
                        registeredId: string,
                        formKey: FormKey) {
    super(modalService, registeredId);
    this.formKey = formKey;
  }

  abstract makeForm(): FormGroup<FormState[FormKey]>;

  ngOnInit(): void {
    this.form = this.makeForm();

    if (environment.hmr) {
      devRestoreFormState(this.fm.query.getValue()[this.formKey], this.form);
      this.fm.upsert(this.formKey, this.form);
      this.modalQuery.modalClosed$(this.registeredId)
        .subscribe(() => this.fm.remove(this.formKey));
    }
  }

  ngOnDestroy(): void {
    if (environment.hmr) {
      this.fm.unsubscribe(this.formKey);
    }
  }
}
