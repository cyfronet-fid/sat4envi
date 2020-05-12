import { OnInit, OnDestroy } from '@angular/core';
import {ModalService} from '../../state/modal.service';
import {FormGroup} from '@ng-stack/forms';
import {environment} from '../../../../environments/environment';
import {devRestoreFormState} from '../../../utils/miscellaneous/miscellaneous';
import {REPORT_MODAL_ID} from '../../../views/map-view/zk/report-modal/report-modal.model';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {FormState} from '../../../state/form/form.model';
import {Observable} from 'rxjs';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {ModalQuery} from '../../state/modal.query';

export class ModalComponent<ReturnType=void> {
  public dismiss(returnValue?: ReturnType): void {
    this.modalService.hide(this.registeredId, returnValue);
  }

  constructor(protected modalService: ModalService, public registeredId?: string) {}
}

export abstract class FormModalComponent<FormKey extends keyof FormState, ReturnType=void> extends ModalComponent<ReturnType> implements OnInit, OnDestroy{
  abstract makeForm(): FormGroup<FormState[FormKey]>;
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
