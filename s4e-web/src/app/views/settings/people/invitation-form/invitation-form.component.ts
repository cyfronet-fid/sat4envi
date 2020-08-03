import { InjectorModule } from 'src/app/common/injector.module';
import { Validators } from '@angular/forms';
import { NotificationService } from './../../../../../../projects/notifications/src/lib/state/notification.service';
import { Institution } from './../../state/institution/institution.model';
import { Modal } from '../../../../modal/state/modal.model';
import { INVITATION_FORM_MODAL_ID, isInvitationFormModal, InvitationFormModal } from './invitation-form-modal.model';
import { ModalQuery } from 'src/app/modal/state/modal.query';
import { ModalService } from 'src/app/modal/state/modal.service';
import {Component, Inject} from '@angular/core';
import {FormControl, FormGroup} from '@ng-stack/forms';
import {InstitutionService} from '../../state/institution/institution.service';
import {ActivatedRoute} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../../../state/form/form.model';
import { FormModalComponent } from 'src/app/modal/utils/modal/modal.component';
import { assertModalType } from 'src/app/modal/utils/modal/misc';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { InstitutionsSearchResultsQuery } from '../../state/institutions-search/institutions-search-results.query';
import { validateAllFormFields } from 'src/app/utils/miscellaneous/miscellaneous';
import { InvitationForm } from './invitation-form.model';
import { Invitation } from '../state/invitation/invitation.model';
import { InvitationService } from '../state/invitation/invitation.service';

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
    private _invitationService: InvitationService,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery,
    private _notificationService: NotificationService,
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
        Validators.required
      )
    });
  }

  send() {
    if (!this.institution) {
      this._notificationService.addGeneral({
        content: `
          Institution isn't selected,
          please refresh page or contact admins if error still occurs
        `,
        type: 'error'
      });
      return;
    }

    validateAllFormFields(this.form);
    if (this.form.invalid) {
      return;
    }

    const email = this.form.controls.email.value;
    !!this.invitation
      ? this._invitationService.resend(this.invitation, this.institution)
      : this._invitationService.create(this.institution.slug, email);
    this.dismiss();
  }
}
