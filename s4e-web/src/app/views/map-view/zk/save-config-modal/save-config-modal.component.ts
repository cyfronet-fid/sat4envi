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

import {
  Component,
  ElementRef,
  Inject,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {FormState} from '../../../../state/form/form.model';
import {ModalService} from '../../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../../modal/modal.providers';
import {Modal} from '../../../../modal/state/modal.model';
import {ModalQuery} from '../../../../modal/state/modal.query';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {environment} from '../../../../../environments/environment';
import {
  devRestoreFormState,
  validateAllFormFields
} from '../../../../utils/miscellaneous/miscellaneous';
import {
  isSaveConfigModal,
  SAVE_CONFIG_MODAL_ID,
  SaveConfigForm
} from './save-config-modal.model';
import {ViewConfigurationEx} from '../../state/view-configuration/view-configuration.model';
import {ViewConfigurationService} from '../../state/view-configuration/view-configuration.service';
import {ViewConfigurationQuery} from '../../state/view-configuration/view-configuration.query';
import {Observable} from 'rxjs';
import {InputComponent} from '../../../../form/input/input.component';

@Component({
  selector: 's4e-save-config-modal',
  templateUrl: './save-config-modal.component.html',
  styleUrls: ['./save-config-modal.component.scss']
})
export class SaveConfigModalComponent
  extends ModalComponent
  implements OnInit, OnDestroy {
  form: FormGroup<SaveConfigForm>;
  formKey: keyof FormState = 'saveConfig';
  loading$: Observable<boolean>;
  error$: Observable<any>;

  @ViewChild('reportTemplate', {read: ElementRef, static: true})
  reportHTML: ElementRef;
  @ViewChild('configurationNameRef', {read: InputComponent, static: true})
  configurationNameRef: InputComponent;
  readonly viewConfig: ViewConfigurationEx;
  includeSceneFc = new FormControl<boolean>(true);

  constructor(
    modalService: ModalService,
    @Inject(MODAL_DEF) modal: Modal,
    private modalQuery: ModalQuery,
    protected fm: AkitaNgFormsManager<FormState>,
    private configurationService: ViewConfigurationService,
    private configurationQuery: ViewConfigurationQuery
  ) {
    super(modalService, modal.id);
    if (!isSaveConfigModal(modal)) {
      throw new Error(`${modal} is not a valid ${SAVE_CONFIG_MODAL_ID}`);
    }
    this.viewConfig = modal.viewConfiguration;
  }

  ngOnInit(): void {
    this.form = new FormGroup<SaveConfigForm>({
      configurationName: new FormControl<string>('', Validators.required)
    });

    setTimeout(() => this.configurationNameRef.focus(), 500);

    this.error$ = this.configurationQuery.selectError();
    this.loading$ = this.configurationQuery.selectLoading();

    if (environment.hmr) {
      devRestoreFormState(this.fm.query.getValue()[this.formKey], this.form);
      this.fm.upsert(this.formKey, this.form);

      this.modalQuery
        .modalClosed$(SAVE_CONFIG_MODAL_ID)
        .subscribe(() => this.fm.remove(this.formKey));
    }
  }

  ngOnDestroy(): void {
    if (environment.hmr) {
      this.fm.unsubscribe(this.formKey);
    }
  }

  async accept() {
    validateAllFormFields(this.form);
    if (this.form.invalid) {
      return;
    }

    if (
      await this.configurationService
        .add$({
          ...this.viewConfig,
          caption: this.form.controls.configurationName.value,
          thumbnail: this.viewConfig.thumbnail.substr(
            'data:image/png;base64,'.length
          ),
          configuration: {
            ...this.viewConfig.configuration,
            manualDate: this.includeSceneFc.value
              ? this.viewConfig.configuration.manualDate
              : undefined,
            date: this.includeSceneFc.value
              ? this.viewConfig.configuration.date
              : undefined,
            sceneId: this.includeSceneFc.value
              ? this.viewConfig.configuration.sceneId
              : undefined
          }
        })
        .toPromise()
    ) {
      this.dismiss();
    }
  }
}
