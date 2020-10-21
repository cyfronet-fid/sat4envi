import { ExpertHelpService } from './../state/expert-help.service';
import { ExpertHelpForm, isExpertHelpModal } from './expert-help-modal.model';
import { Component, Inject } from '@angular/core';
import { FormModalComponent } from 'src/app/modal/utils/modal/modal.component';
import { ModalService } from 'src/app/modal/state/modal.service';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { Modal } from 'src/app/modal/state/modal.model';
import { ModalQuery } from 'src/app/modal/state/modal.query';
import { AkitaNgFormsManager } from '@datorama/akita-ng-forms-manager';
import { FormState } from 'src/app/state/form/form.model';
import { NotificationService, GeneralNotification } from 'notifications';
import { assertModalType } from 'src/app/modal/utils/modal/misc';
import { FormGroup, FormControl, Validators } from '@ng-stack/forms';
import { validateAllFormFields } from 'src/app/utils/miscellaneous/miscellaneous';

@Component({
  selector: 's4e-expert-help-modal',
  templateUrl: './expert-help-modal.component.html',
  styleUrls: ['./expert-help-modal.component.scss']
})
export class ExpertHelpModalComponent extends FormModalComponent<'expertHelp'> {
  constructor(
    modalService: ModalService,
    @Inject(MODAL_DEF) modal: Modal,
    modalQuery: ModalQuery,
    fm: AkitaNgFormsManager<FormState>,

    private _notificationService: NotificationService,
    private _expertHelpService: ExpertHelpService
  ) {
    super(fm, modalService, modalQuery, modal.id, 'expertHelp');

    assertModalType(isExpertHelpModal, modal);
  }

  makeForm(): FormGroup<FormState['expertHelp']> {
    return new FormGroup<ExpertHelpForm>({
      helpType: new FormControl<string>(null, [Validators.required]),
      issueDescription: new FormControl<string>(null, [Validators.required])
    });
  }

  hasErrors(controlName: string) {
    const formControl = this.form
      .controls[controlName] as FormControl;
    return !!formControl
      && formControl.touched
      && !!formControl.errors
      && Object.keys(formControl.errors).length > 0;
  }

  async sendIssue$() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});

    if (this.form.invalid) {
      return;
    }

    if (await this.modalService.confirm(
      'Wsparcie eksperckie',
      'Czy na pewno chcesz wysłać prośbę o wsparcie zdalne?'
    )) {
      this._expertHelpService.sendHelpRequest$(this.form.value)
      .subscribe(() => {
        this._notificationService.addGeneral({
          content: 'Prośba o wsparcie eksperckie została wysłana',
          type: 'success'
        });
        this.dismiss();
      });
    }
  }
}
