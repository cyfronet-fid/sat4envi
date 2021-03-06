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

import {Validators} from '@angular/forms';
import {Institution} from '../../state/institution/institution.model';
import {
  INVITATION_FORM_MODAL_ID,
  isInvitationFormModal,
  InvitationFormModal
} from './invitation-form-modal.model';
import {ModalQuery} from 'src/app/modal/state/modal.query';
import {ModalService} from 'src/app/modal/state/modal.service';
import {Component, Inject} from '@angular/core';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {InstitutionService} from '../../state/institution/institution.service';
import {ActivatedRoute} from '@angular/router';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import {FormModalComponent} from 'src/app/modal/utils/modal/modal.component';
import {assertModalType} from 'src/app/modal/utils/modal/misc';
import {MODAL_DEF} from 'src/app/modal/modal.providers';
import {InstitutionsSearchResultsQuery} from '../../state/institutions-search/institutions-search-results.query';
import {validateAllFormFields} from 'src/app/utils/miscellaneous/miscellaneous';
import {InvitationForm} from './invitation-form.model';
import {Invitation} from '../state/invitation/invitation.model';
import {InvitationService} from '../state/invitation/invitation.service';
import {InvitationQuery} from '../state/invitation/invitation.query';
import {NotificationService} from '../../../../notifications/state/notification.service';

@Component({
  templateUrl: './invitation-form.component.html',
  styleUrls: ['./invitation-form.component.scss']
})
export class InvitationFormComponent extends FormModalComponent<'invitation'> {
  form: FormGroup<InvitationForm>;
  modalId = INVITATION_FORM_MODAL_ID;

  institution: Institution;
  invitation: Invitation | null;

  constructor(
    fm: AkitaNgFormsManager<FormState>,
    private _institutionService: InstitutionService,
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _invitationService: InvitationService,
    private _route: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery,
    private _notificationService: NotificationService,
    private _invitationQuery: InvitationQuery,
    @Inject(MODAL_DEF) modal: InvitationFormModal
  ) {
    super(fm, _modalService, _modalQuery, INVITATION_FORM_MODAL_ID, 'invitation');
    assertModalType(isInvitationFormModal, modal);

    this.institution = modal.institution;
    this.invitation = modal.invitation;
  }

  makeForm(): FormGroup<InvitationForm> {
    return new FormGroup<InvitationForm>({
      email: new FormControl<string>(
        !!this.invitation ? this.invitation.email : null,
        [Validators.required, Validators.email]
      ),
      forAdmin: new FormControl<boolean>(false)
    });
  }

  send() {
    validateAllFormFields(this.form);
    if (this.form.invalid) {
      return;
    }

    const {email, forAdmin} = this.form.value;
    const invitationExist = !!this._invitationQuery.getEntity(email);
    !!this.invitation || invitationExist
      ? this._invitationService.resend(
          {oldEmail: email, newEmail: email, forAdmin},
          this.institution
        )
      : this._invitationService
          .send(this.institution.slug, email, forAdmin)
          .subscribe();
    this.dismiss();
  }
}
