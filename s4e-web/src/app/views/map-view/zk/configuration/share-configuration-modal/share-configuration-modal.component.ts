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

import { filter } from 'rxjs/operators';
import {Component, Inject} from '@angular/core';
import {FormModalComponent} from '../../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../../modal/state/modal.service';
import {MODAL_DEF} from '../../../../../modal/modal.providers';
import {ModalQuery} from '../../../../../modal/state/modal.query';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../../state/form/form.model';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {ConfigurationModal, isConfigurationModal, ShareConfigurationForm} from '../state/configuration.model';
import {Base64Image} from '../../../../../common/types';
import {assertModalType} from '../../../../../modal/utils/modal/misc';
import {DOCUMENT} from '@angular/common';
import {validateAllFormFields} from '../../../../../utils/miscellaneous/miscellaneous';
import {ValidatorFn} from '@ng-stack/forms/lib/types';
import {AbstractControl} from '@angular/forms';
import {ConfigurationService} from '../state/configuration.service';
import {ConfigurationQuery} from '../state/configuration.query';

const EMAIL_REGEXP = /^[a-z0-9!#$%&'*+/=?^_`{|}~.-]+@[a-z0-9-]+(\.[a-z0-9-]+)*$/i;

export const EmailListValidator: ValidatorFn<{ email: true }> = (control: AbstractControl) => {
  // run EMAIL_REGEXP over every email in string, if there is one which does not match return error dict
  if ((control.value || '').split(',').map(s => s.trim().match(EMAIL_REGEXP)).indexOf(null) != -1) {
    return {email: true};
  }
  return null;
};

@Component({
  selector: 's4e-share-configuration-modal',
  templateUrl: './share-configuration-modal.component.html',
  styleUrls: ['./share-configuration-modal.component.scss']
})
export class ShareConfigurationModalComponent extends FormModalComponent<'configurationShare'> {
  public image: Base64Image = '';
  public configurationUrl: string = '';
  public displayHref: string = '';

  constructor(modalService: ModalService,
              @Inject(MODAL_DEF) modal: ConfigurationModal,
              modalQuery: ModalQuery,
              private configurationService: ConfigurationService,
              private configurationQuery: ConfigurationQuery,
              @Inject(DOCUMENT) document: Document,
              fm: AkitaNgFormsManager<FormState>) {
    super(fm, modalService, modalQuery, modal.id, 'configurationShare');
    assertModalType(isConfigurationModal, modal);
    this.image = modal.mapImage;
    this.displayHref = document.location.origin + modal.configurationUrl;
    this.configurationUrl = modal.configurationUrl;
    this.isLoading$ = this.configurationQuery.selectLoading();
  }

  makeForm(): FormGroup<ShareConfigurationForm> {
    return new FormGroup<ShareConfigurationForm>({
      caption: new FormControl<string>('', Validators.required),
      description: new FormControl<string>(null, Validators.required),
      emails: new FormControl<string>('', [Validators.required, EmailListValidator]),
    });
  }

  submit() {
    validateAllFormFields(this.form);
    if (this.form.invalid) {
      return;
    }

    const configuration = {
      caption: this.form.value.caption,
      description: this.form.value.description,
      emails: this.form.value.emails.split(',').map(s => s.trim()),
      path: this.configurationUrl,
      thumbnail: this.image.replace(/^data:image\/[a-z]+;base64,/, '')
    };
    this.configurationService.shareConfiguration(configuration)
      .pipe(filter(value => value))
      .subscribe(() => this.dismiss());
  }
}
