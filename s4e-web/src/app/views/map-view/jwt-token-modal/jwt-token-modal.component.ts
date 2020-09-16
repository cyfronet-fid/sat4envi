import { NotificationService } from './../../../../../projects/notifications/src/lib/state/notification.service';
import { Observable } from 'rxjs';
import { SessionQuery } from './../../../state/session/session.query';
import { SessionService } from './../../../state/session/session.service';
import { JwtTokenForm } from 'src/app/views/map-view/jwt-token-modal/jwt-token-modal.model';
import { Modal } from './../../../modal/state/modal.model';
import { Component, OnInit, Inject } from '@angular/core';
import { ModalService } from 'src/app/modal/state/modal.service';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { ModalQuery } from 'src/app/modal/state/modal.query';
import { AkitaNgFormsManager } from '@datorama/akita-ng-forms-manager';
import { FormState } from 'src/app/state/form/form.model';
import { FormModalComponent } from 'src/app/modal/utils/modal/modal.component';
import { assertModalType } from 'src/app/modal/utils/modal/misc';
import { isJwtTokenModal } from './jwt-token-modal.model';
import { FormGroup, FormControl, Validators } from '@ng-stack/forms';
import { validateAllFormFields } from 'src/app/utils/miscellaneous/miscellaneous';

@Component({
  selector: 's4e-jwt-token-modal',
  templateUrl: './jwt-token-modal.component.html',
  styleUrls: ['./jwt-token-modal.component.scss']
})
export class JwtTokenModalComponent extends FormModalComponent<'jwtToken'> {
  public token: string;

  constructor(
    modalService: ModalService,
    @Inject(MODAL_DEF) modal: Modal,
    modalQuery: ModalQuery,
    fm: AkitaNgFormsManager<FormState>,

    private _sessionService: SessionService,
    private _sessionQuery: SessionQuery,
    private _notificationService: NotificationService
  ) {
    super(fm, modalService, modalQuery, modal.id, 'jwtToken');

    assertModalType(isJwtTokenModal, modal);
  }

  makeForm(): FormGroup<FormState['jwtToken']> {
    return new FormGroup<JwtTokenForm>({
      password: new FormControl<string>(null, [Validators.required]),
    });
  }

  copyToClipboard() {
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = this.token;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);

    this._notificationService.addGeneral({
      content: 'Token pomyślnie skopiowano do schowka',
      type: 'success'
    });
  }

  async getJwtToken() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});

    if (this.form.invalid) {
      return;
    }

    const email = this._sessionQuery.getValue().email;
    const password = this.form.controls.password.value;
    this.token = await this._sessionService
      .getJwtToken$({email, password})
      .toPromise();
  }
}
